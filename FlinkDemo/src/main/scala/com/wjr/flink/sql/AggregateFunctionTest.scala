package com.wjr.flink.sql

import com.wjr.flink.stream.TempSensor
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{DataTypes, Schema}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.AggregateFunction
import org.apache.flink.types.Row

import java.time.Duration
import scala.util.Random
// imports for Table API with bridging to Scala DataStream API
import org.apache.flink.table.api._
// imports for Scala DataStream API
import org.apache.flink.api.scala._

/**
 * @Package: com.wjr.flink.sql
 * @ClassName: AggregateFunctionTest
 * @author 29375-wjr
 * @create 2022-04-26 20:25
 * @Description: 多对一
 */
object AggregateFunctionTest {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)
    val tableEnv = StreamTableEnvironment.create(env)

    val inputFile = env.readTextFile("E:\\All_Project\\IDEA workspace\\SmartStreetLamp\\FlinkDemo\\src\\main\\resources\\wordCount.txt")


    val tempSource = inputFile.map(data => {
      val strings = data.split(" ")
      TempSensor(strings(0), System.currentTimeMillis(), Random.nextGaussian() + 20)
    })
      .assignTimestampsAndWatermarks(WatermarkStrategy
        // .forMonotonousTimestamps()//单调递增策略
        // .noWatermarks()//不生成策略
        .forBoundedOutOfOrderness(Duration.ofMillis(5)) //固定乱序长度策略
        .withTimestampAssigner(new SerializableTimestampAssigner[TempSensor] {
          override def extractTimestamp(element: TempSensor, recordTimestamp: Long): Long = {
            element.timestamp
          }
        }))
    val sensorTable2 = tableEnv.fromDataStream(tempSource, Schema.newBuilder()
      .column("id", DataTypes.of(classOf[String]))
      .column("timestamp", DataTypes.of(classOf[Long]))
      .column("temperature", DataTypes.of(classOf[Double]))
      .columnByExpression("proc_time", "PROCTIME()") // 添加的processTime时间列
      .columnByMetadata("rowtime", "TIMESTAMP_LTZ(3)") // 添加的时间列
      .watermark("rowtime", "SOURCE_WATERMARK()") // 使用watermark，DS必须使用Wartermark策略
      .build())
    sensorTable2.printSchema()
    tableEnv.toRetractStream[Row](sensorTable2).print("sensorTable2")
    tableEnv.createTemporaryView("sensorTable2", sensorTable2)

    // 使用聚合函数
    val avg = new WeightedAvg()
    sensorTable2.groupBy($"id").aggregate(avg($"temperature",1) as "avgTemp")
      .select($"id",$"avgTemp")
      .execute().print()

    sensorTable2.groupBy($"id")
      .select($"id",call(avg,$"temperature",1) as "avgTemp")
      .execute().print()

    // register function
    tableEnv.createTemporarySystemFunction("WeightedAvg", classOf[WeightedAvg])
    // call registered function in SQL
    tableEnv.sqlQuery(
      "SELECT id, WeightedAvg(`temperature`, 1) as avgTemp FROM sensorTable2 GROUP BY id"
    ).execute().print()

    env.execute("AggregateFunctionTest")
  }

}

// mutable accumulator of structured type for the aggregate function
case class WeightedAvgAccumulator(
                                   var sum: java.lang.Double = 0, // 和
                                   var count: java.lang.Integer = 0 // 数量
                                 )

/**
 * 自定义聚合聚合函数，求每个传感器的平均温度值
 * AggregateFunction[T,ACC]
 * T:输出类型
 * ACC:累加器初始状态
 */
class WeightedAvg extends AggregateFunction[java.lang.Double, WeightedAvgAccumulator] {

  /**
   * 创建一个空累加器
   *
   * @return
   */
  override def createAccumulator(): WeightedAvgAccumulator = {
    WeightedAvgAccumulator()
  }

  /**
   * 计算并返回最终结果。
   *
   * @param acc
   * @return
   */
  override def getValue(acc: WeightedAvgAccumulator): java.lang.Double = {
    if (acc.count == 0) {
      0
    } else {
      acc.sum / acc.count
    }
  }

  /**
   * 每一条数据，更新累加器
   *
   * @param acc 累加器
   * @param temperature 温度值
   * @param iWeight 权重
   */
  def accumulate(acc: WeightedAvgAccumulator, temperature: java.lang.Double, iWeight: java.lang.Integer): Unit = {
    acc.sum += temperature * iWeight
    acc.count += iWeight
  }

  def retract(acc: WeightedAvgAccumulator, iValue: java.lang.Double, iWeight: java.lang.Integer): Unit = {
    acc.sum -= iValue * iWeight
    acc.count -= iWeight
  }

  def merge(acc: WeightedAvgAccumulator, it: java.lang.Iterable[WeightedAvgAccumulator]): Unit = {
    val iter = it.iterator()
    while (iter.hasNext) {
      val a = iter.next()
      acc.count += a.count
      acc.sum += a.sum
    }
  }

  def resetAccumulator(acc: WeightedAvgAccumulator): Unit = {
    acc.count = 0
    acc.sum = 0L
  }
}
