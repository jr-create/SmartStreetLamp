package com.wjr.flink.stream

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkGenerator, WatermarkGeneratorSupplier, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.assigners.{EventTimeSessionWindows, SlidingEventTimeWindows, SlidingProcessingTimeWindows, TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.compact
import org.json4s.{DefaultFormats, Extraction}

import java.time.Duration
import java.util.Properties
import scala.util.Random

/**
 * @Package: com.wjr.flink.stream
 * @ClassName: WindowsAPITest
 * @author 29375-wjr
 * @create 2022-04-04 22:40
 * @Description:
 */
object WindowsAPITest {
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setParallelism(2)
  //
  env.getConfig.setAutoWatermarkInterval(10L)

  // kafka数据源
  val properties = new Properties()
  properties.put("bootstrap.servers", "42.192.65.44:9092")
  properties.setProperty("group.id", "flink-window")
  val myConsumer = new FlinkKafkaConsumer[String]("flink", new SimpleStringSchema(), properties)
  val inputStream: DataStream[String] = env.addSource(myConsumer)
  val tempSensorSource = inputStream.map(data => {
    implicit val formats = DefaultFormats
    val values = data.split(" ")
    TempSensor(values(0), System.currentTimeMillis(), values(1).toDouble)
  })
  val dataStream = tempSensorSource.map(data => {
    implicit val formats = DefaultFormats
    val str = compact(Extraction.decompose(data))
    println("输入JSON数据：" + str)
    str
  })

  def main(args: Array[String]): Unit = {
    /**
     * 使用watermark
     */
    val watermarkResult = tempSensorSource.assignTimestampsAndWatermarks(WatermarkStrategy
      // .forMonotonousTimestamps()//单调递增策略
      // .noWatermarks()//不生成策略
      .forBoundedOutOfOrderness(Duration.ofMillis(5)) //固定乱序长度策略
      .withTimestampAssigner(new SerializableTimestampAssigner[TempSensor] {
        override def extractTimestamp(element: TempSensor, recordTimestamp: Long): Long = {
          element.timestamp
        }
      })).keyBy(_.id)
      .window(TumblingEventTimeWindows.of(Time.seconds(15))) //EventTime需要和watermark连用
      .allowedLateness(Time.seconds(1L)) //窗口延迟关闭
      .sideOutputLateData(new OutputTag[TempSensor]("id")) //侧输出流,兜底保证，窗口在关闭后，把数据输出到侧输出流，之后看到late的数据可以进行和之前的数据进行手动合并
      .reduce((r1, r2) => TempSensor(r2.id, r2.timestamp, r1.temperature.min(r2.temperature)))

    watermarkResult.print("watermark")

    val result = dataStream.map(x => {
      import org.json4s._
      implicit val formats = DefaultFormats
      val sensor = JsonMethods.parse(x).extract[TempSensor]
      (sensor.id, sensor.temperature)
    }).keyBy(_._1)

      .window(TumblingProcessingTimeWindows.of(Time.seconds(15))) //滚动时间窗口
      // .window(SlidingEventTimeWindows.of(Time.seconds(14),Time.seconds(5)))//滑动时间窗口
      // .window(EventTimeSessionWindows.withGap(Time.seconds(14))) //会话窗口
      // .countWindow(14)//计数窗口 当达到数量后进行触发
      .reduce((r1, r2) => (r1._1, r1._2.min(r2._2)))

    result.print("result")

    env.execute()
  }

}
