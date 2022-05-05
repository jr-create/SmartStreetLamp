package com.wjr.flink.sql

import com.wjr.flink.stream.TempSensor
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{DataTypes, Schema}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.TableAggregateFunction
import org.apache.flink.types.Row
import org.apache.flink.util.Collector
import org.apache.flink.api.java.tuple.Tuple2
import java.time.Duration
import scala.util.Random
// imports for Table API with bridging to Scala DataStream API
import org.apache.flink.table.api._
// imports for Scala DataStream API
import org.apache.flink.api.scala._

/**
 * @Package: com.wjr.flink.sql
 * @ClassName: TableAggregateFunctionTest
 * @author 29375-wjr
 * @create 2022-04-26 21:30
 * @Description:
 */
object TableAggregateFunctionTest {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
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

    // 使用表聚合函数
    val top2 = new Top2()
    sensorTable2.groupBy($"id")
      .flatAggregate(top2($"temperature") as("temperature", "top2"))
      .select($"id", $"temperature", $"top2")
      .execute().print()

    // register function
    tableEnv.createTemporarySystemFunction("WeightedAvg", classOf[WeightedAvg])

    val top2Table = sensorTable2.groupBy($"id")
      .flatAggregate(call(top2, $"temperature") as("temperature", "top2"))
      .select($"id", $"temperature", $"top2")
    tableEnv.toRetractStream[Row](top2Table).print("top2Table")


    env.execute()

  }

}

// mutable accumulator of structured type for the aggregate function
case class Top2Accumulator(
                            var firstTemp: java.lang.Double,
                            var secondTemp: java.lang.Double
                          )

// 自定义表聚合函数，提取所有温度值中最高的两个温度
// function that takes (value INT), stores intermediate results in a structured
// type of Top2Accumulator, and returns the result as a structured type of Tuple2[java.lang.Double, java.lang.Double]
// for value and rank
class Top2 extends TableAggregateFunction[Tuple2[java.lang.Double, java.lang.Double], Top2Accumulator] {

  override def createAccumulator(): Top2Accumulator = {
    Top2Accumulator(
      java.lang.Double.MIN_VALUE,
      java.lang.Double.MIN_VALUE
    )
  }

  def accumulate(acc: Top2Accumulator, temperature: java.lang.Double): Unit = {
    if (temperature > acc.firstTemp) {
      acc.secondTemp = acc.firstTemp
      acc.firstTemp = temperature
    } else if (temperature > acc.secondTemp) {
      acc.secondTemp = temperature
    }
  }

  def merge(acc: Top2Accumulator, it: java.lang.Iterable[Top2Accumulator]) {
    val iter = it.iterator()
    while (iter.hasNext) {
      val otherAcc = iter.next()
      accumulate(acc, otherAcc.firstTemp)
      accumulate(acc, otherAcc.secondTemp)
    }
  }

  // 调用函数的 emitValue(...) 或 emitUpdateWithRetract(...) 方法来计算并返回最终结果。
  def emitValue(acc: Top2Accumulator, out: Collector[Tuple2[java.lang.Double, java.lang.Double]]): Unit = {
    // emit the value and rank
    if (acc.firstTemp != java.lang.Double.MIN_VALUE) {
      out.collect(Tuple2.of(acc.firstTemp, 1))
    }
    if (acc.secondTemp != java.lang.Double.MIN_VALUE) {
      out.collect(Tuple2.of(acc.secondTemp, 2))
    }
  }
}
