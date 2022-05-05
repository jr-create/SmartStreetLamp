package com.wjr.flink.sql

import com.wjr.flink.stream.TempSensor
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.annotation.{DataTypeHint, FunctionHint}
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.table.functions.TableFunction
import org.apache.flink.types.Row

import java.time.Duration
import scala.util.Random
// imports for Table API with bridging to Scala DataStream API
import org.apache.flink.table.api._
// imports for Scala DataStream API
import org.apache.flink.api.scala._

/**
 * @Package: com.wjr.flink.sql
 * @ClassName: TableFunctionTest
 * @author 29375-wjr
 * @create 2022-04-26 9:16
 * @Description:
 */
object TableFunctionTest {
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
    // 定义实例
    val split = new SplitTableFunction("_")
    // 在运行环境中使用，通过call直接使用方法
    sensorTable2
      .joinLateral(call(split, $"id").as("word", "length"))
      .select($"id", $"rowtime", $"word", $"length")
      .execute().print()

    // 使用SQL，需要先注册方法
    // 1. register function
    tableEnv.createTemporarySystemFunction("split", split)
    // 2. call registered function in SQL
    tableEnv
      .sqlQuery(
        """
          |SELECT id,rowtime,word,length
          |FROM sensorTable2, LATERAL TABLE(split(id)) as T(word,length)
          |""".stripMargin)
      .execute().print()
  }

}

/**
 * 自定义表函数
 */
// Scala tuples are not supported. Use case classes or 'org.apache.flink.types.Row' instead.
@FunctionHint(output = new DataTypeHint("ROW<word STRING, length INT>"))
class SplitTableFunction(separator: String) extends TableFunction[Row] {
  def eval(s: String): Unit = {
    s.split(separator).foreach(word => collect(Row.of(word, Int.box(word.length))))
  }
}
