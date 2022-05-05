package com.wjr.flink.sql

import com.wjr.flink.stream.TempSensor
import com.wjr.flink.stream.WindowsAPITest.env
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.json4s.DefaultFormats

import java.time.Duration
import java.util.Properties
import scala.util.Random
// imports for Table API with bridging to Scala DataStream API
import org.apache.flink.table.api._
// imports for Scala DataStream API
import org.apache.flink.api.scala._

/**
 * @Package: com.wjr.flink.sql
 * @ClassName: TimeTableTest
 * @author 29375-wjr
 * @create 2022-04-24 22:35
 * @Description:
 */
object TimeTableTest {

  val kafkaSourceSql =
    """
      |CREATE TABLE kafkaTable (
      |  json STRING
      |) WITH (
      |  'connector' = 'kafka',
      |  'topic' = 'flink',
      |  'properties.bootstrap.servers' = '124.220.2.188:9092',
      |  'properties.group.id' = 'testGroup',
      |  'scan.startup.mode' = 'earliest-offset',
      |  'format' = 'json',
      |  'json.fail-on-missing-field' = 'false',
      |  'json.ignore-parse-errors' = 'true'
      |)
      |""".stripMargin

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getConfig.setAutoWatermarkInterval(10L)
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

    // val sensorTable1 = tableEnv.fromDataStream(tempSource, $"id", $"timestamp", $"temperature", $"pt".proctime())
    // sensorTable1.printSchema()
    // sensorTable1.execute().print()

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
    // sensorTable2.execute().print()

    // Group Window
    // 需求：根据ID进行分组，求每个Id的个数/平均温度值
    sensorTable2.window(Tumble over 10.second on $"rowtime" as $"w")
      .groupBy($"id", $"w") // 窗口必须作为groupby的字段
      .select($"id", $"id".count(), $"temperature".avg(), $"w".end()) // 每个Id的个数/平均温度值/窗口的关窗时间
      .execute().print()

    // 使用SQL查询
    tableEnv.createTemporaryView("sensorTable2", sensorTable2)
    val windowGroupSQL =
      """
        |select id,count(id),avg(temperature),tumble_end(rowtime, interval '10' second)
        |from sensorTable2
        |group by id,tumble(rowtime, interval '10' second)
        |""".stripMargin
    tableEnv.executeSql(windowGroupSQL).print()
    // overWindow
    /**
     * 无界数据 partitionBy、orderBy、preceding、following
       * preceding->
       *    UNBOUNDED_RANGE:时间范围
       *    UNBOUNDED_ROW：行数
     * 有界数据 partitionBy、orderBy、preceding、following
       * preceding->
       *    1.minutes
       *    10.rows
     * SQL:
     *    select count(id) over(partition by id order by rowtime rows between 2 preceding and current row)
     *    from sensorTable2
     */
    val overTable = sensorTable2.window(Over partitionBy $"id" orderBy $"rowtime" preceding UNBOUNDED_RANGE as $"rk")
      .select($"id", $"rowtime", $"id".count().over($"rk").as("countId"), $"temperature".avg().over($"rk").as("avgTemp"))
    overTable.execute().print()

    env.execute("TimeTable")
  }

}
