package com.wjr.flink.stream

import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapFunction}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.restartstrategy.RestartStrategies.RestartStrategyConfiguration
import org.apache.flink.api.common.state.{ListStateDescriptor, MapState, MapStateDescriptor, ReducingState, ReducingStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.scala.createTypeInformation
import org.apache.flink.util.Collector
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend
import org.apache.flink.contrib.streaming.state.{EmbeddedRocksDBStateBackend, RocksDBStateBackend}
import org.apache.flink.runtime.state.memory.MemoryStateBackend
import org.apache.flink.runtime.state.storage.JobManagerCheckpointStorage
import org.apache.flink.streaming.api.CheckpointingMode

import java.util.concurrent.TimeUnit

/**
 * @Package: com.wjr.flink.stream
 * @ClassName: StateTest
 * @author 29375-wjr
 * @create 2022-04-10 15:39
 * @Description:
 */
object StateTest {
  def main(args: Array[String]): Unit = {
    val env = WindowsAPITest.env
    env.setParallelism(1)
    // checkpoint
    env.enableCheckpointing(1000L)//JobManager给slots任务触发checkpoint的时间间隔
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)//默认exactly-once模式
    env.getCheckpointConfig.setCheckpointTimeout(60000L)//checkpoint的超时时间为1分钟
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(2)//checkpoint的并行度，默认为1
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500L)//设置并行checkpoint之间的的最小时间
    env.getCheckpointConfig.setTolerableCheckpointFailureNumber(12)//容忍多少checkpoint失败
    //
    /** 重启策略
     * noRestart
     * fallBackRestart
     * fixedDelayRestart: 固定时间间隔重启
     *    restartAttempts – 重启次数
     *    delayBetweenAttempts - 连续重启的时间间隔
     * failureRateRestart：
     *    failureRate – 重启次数
     *    failureInterval – 失败的时间间隔
     *    delayInterval – 连续重启的时间间隔
     * exponentialDelayRestart：
     *
     */
    // env.setRestartStrategy(RestartStrategies.fixedDelayRestart(2,4000L))
    // env.setRestartStrategy(RestartStrategies.failureRateRestart(3,Time.of(4,TimeUnit.MINUTES),Time.of(4,TimeUnit.SECONDS)))

    // 状态后端
    // env.setStateBackend(new MemoryStateBackend())//过时，请使用下面两行
    // env.setStateBackend(new HashMapStateBackend)
    // env.getCheckpointConfig.setCheckpointStorage(new JobManagerCheckpointStorage)

    // env.setStateBackend(new FsStateBackend())//过时，请使用下面两行
    // env.setStateBackend(new HashMapStateBackend)
    // env.getCheckpointConfig.setCheckpointStorage("hdfs:///checkpoints")

    // env.setStateBackend(new RocksDBStateBackend())//过时，请使用下面两行
    // env.setStateBackend(new EmbeddedRocksDBStateBackend)
    // env.getCheckpointConfig.setCheckpointStorage("hdfs://checkpoints")
    val source = WindowsAPITest.tempSensorSource

    /**
     * 需求：温度值突然跳变10度，需要报警
     */
    val alertStream1 = source.keyBy(_.id).flatMap(new TempChangeAlert(1.0))
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
    val alertStreamMap = source.keyBy(_.id).map(new TempChangeAlertMap(1.0))

    alertStream1.print("alertStream1")
    alertStream2.print("alertStream2")
    alertStreamMap.print("alertStreamMap")

    env.execute("StateTest")
  }

}

class TempChangeAlertMap(threshold: Double) extends RichMapFunction[TempSensor, (String, Double, Double)] {

  //定义状态，保存上一个温度值
  lazy val lastTempState: ValueState[Double] = getRuntimeContext.getState(new ValueStateDescriptor[Double]("temperaStateMap", classOf[Double]))

  override def map(value: TempSensor): (String, Double, Double) = {
    val lastTemp = lastTempState.value() //获取上一次的值
    // 与最新温度值进行比较
    val diffTemp = (value.temperature - lastTemp).abs
    if (diffTemp >= threshold) {
      // 更新状态
      lastTempState.update(value.temperature)
    }
    // 输出结果
    (value.id, lastTemp, value.temperature)
  }
}

/**
 * 实现自定义flatMap方法，RichFlatMap
 */
class TempChangeAlert(threshold: Double) extends RichFlatMapFunction[TempSensor, (String, Double, Double)] {
  //定义状态，保存上一个温度值
  lazy val lastTempState: ValueState[Double] = getRuntimeContext.getState(new ValueStateDescriptor[Double]("temperaState", classOf[Double]))

  override def flatMap(value: TempSensor, out: Collector[(String, Double, Double)]): Unit = {
    val lastTemp = lastTempState.value() //获取上一次的值
    // 与最新温度值进行比较
    val diffTemp = (value.temperature - lastTemp).abs
    if (diffTemp >= threshold) {
      // 输出结果
      out.collect((value.id, lastTemp, value.temperature))
      // 更新状态
      lastTempState.update(value.temperature)
    }
  }
}

/**
 * keyed State 必须定义在RichFunction中，因为需要运行时上下文
 */
class MyStateRichMapper extends RichMapFunction[TempSensor, String] {

  // lazy val valueState = getRuntimeContext.getState(new ValueStateDescriptor[Double]("valuestate", classOf[Double]))
  var valueState: ValueState[Double] = _
  lazy val listState = getRuntimeContext.getListState(new ListStateDescriptor[Int]("liststate", classOf[Int]))
  lazy val mapState: MapState[String, Double] = getRuntimeContext.getMapState(new MapStateDescriptor[String, Double]("mapstate", classOf[String], classOf[Double]))
  lazy val reduceState: ReducingState[TempSensor] = getRuntimeContext.getReducingState(new ReducingStateDescriptor[TempSensor]("reducestate", (r1, r2) => {
    TempSensor(r2.id, r2.timestamp, r1.temperature.min(r2.temperature))
  }, classOf[TempSensor]))

  override def open(parameters: Configuration): Unit = {
    valueState = getRuntimeContext.getState(new ValueStateDescriptor[Double]("valuestate", classOf[Double]))
  }

  override def map(tempSensor: TempSensor): String = {
    val value = valueState.value()
    println(s"$valueState -> $value")
    valueState.update(tempSensor.temperature)

    listState.add(1)

    mapState.contains("sensor")
    mapState.get("sensor")
    mapState.put(tempSensor.id, 1)

    reduceState.add(tempSensor)
    println(reduceState.get())
    tempSensor.id
  }
}
