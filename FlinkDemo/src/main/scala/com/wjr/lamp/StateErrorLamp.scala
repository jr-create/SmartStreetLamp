package com.wjr.lamp

import com.wjr.flink.stream.WindowsAPITest.env
import com.wjr.flink.stream.{TempChangeAlertMap, TempSensor, WindowsAPITest}
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.util.Collector
import org.json4s.DefaultFormats

import java.util.Properties

// imports for Table API with bridging to Scala DataStream API
// imports for Scala DataStream API
import org.apache.flink.api.scala._
/**
 * @Package: com.wjr.lamp
 * @ClassName: StateErrorLamp
 * @author 29375-wjr
 * @create 2022-05-01 18:16
 * @Description:
 */
object StateErrorLamp {
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setParallelism(2)
  env.getConfig.setAutoWatermarkInterval(10L)

  // checkpoint
  env.enableCheckpointing(1000L)//JobManager给slots任务触发checkpoint的时间间隔
  env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)//默认exactly-once模式
  env.getCheckpointConfig.setCheckpointTimeout(60000L)//checkpoint的超时时间为1分钟
  env.getCheckpointConfig.setMaxConcurrentCheckpoints(2)//checkpoint的并行度，默认为1
  env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500L)//设置并行checkpoint之间的的最小时间
  env.getCheckpointConfig.setTolerableCheckpointFailureNumber(12)//容忍多少checkpoint失败

  env.setRestartStrategy(RestartStrategies.fixedDelayRestart(2,4000L))
  // env.setRestartStrategy(RestartStrategies.failureRateRestart(3,Time.of(4,TimeUnit.MINUTES),Time.of(4,TimeUnit.SECONDS)))

  // 状态后端
  // env.setStateBackend(new FsStateBackend())//过时，请使用下面两行
  // env.setStateBackend(new HashMapStateBackend)
  // env.getCheckpointConfig.setCheckpointStorage("hdfs:///checkpoints")

  // env.setStateBackend(new RocksDBStateBackend())//过时，请使用下面两行
  // env.setStateBackend(new EmbeddedRocksDBStateBackend)
  // env.getCheckpointConfig.setCheckpointStorage("hdfs://checkpoints")

  // kafka数据源
  val properties = new Properties()
  properties.put("bootstrap.servers", "124.220.2.188:9092")
  properties.setProperty("group.id", "flink-window")
  val myConsumer = new FlinkKafkaConsumer[String]("flink", new SimpleStringSchema(), properties)
  val inputStream: DataStream[String] = env.addSource(myConsumer)
  val tempSensorSource = inputStream.map(data => {
    implicit val formats = DefaultFormats
    val values = data.split(" ")
    TempSensor(values(0), System.currentTimeMillis(), values(1).toDouble)
  })
  val source = tempSensorSource

  /**
   * 需求：温度值突然跳变10度，需要报警
   */
  val alertStream2 = source.keyBy(_.id).flatMapWithState[(String, Double, Double), Double] {
    case (tempSensor: TempSensor, None) => (List.empty, Some(tempSensor.temperature))
    case (tempSensor: TempSensor, lastTemp: Some[Double]) =>
      // 与最新温度值进行比较
      val diffTemp = (tempSensor.temperature - lastTemp.get).abs
      if (diffTemp >= 1.0) {
        // 输出结果
        (List((tempSensor.id, lastTemp.get, tempSensor.temperature)), Some(tempSensor.temperature))
      } else {
        (List.empty, Some(tempSensor.temperature))
      }
  }
  alertStream2.print("alertStream2")

  env.execute("StateTest")
}
