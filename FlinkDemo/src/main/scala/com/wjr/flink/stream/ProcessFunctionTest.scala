package com.wjr.flink.stream

import akka.serialization.NullSerializer
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkGenerator, WatermarkGeneratorSupplier, WatermarkStrategy}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeutils.TypeSerializer
import org.apache.flink.api.java.typeutils.runtime.ValueSerializer
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction.StateSerializer
import org.apache.flink.streaming.api.scala.createTypeInformation
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

import java.time.Duration

/**
 * @Package: com.wjr.flink
 * @ClassName: ProcessFunctionTest
 * @author 29375-wjr
 * @create 2022-04-14 8:43
 * @Description:
 */
object ProcessFunctionTest {
  def main(args: Array[String]): Unit = {
    val env = WindowsAPITest.env
    val sensorSource = WindowsAPITest.tempSensorSource

    // sensorSource.keyBy(_.id).process(new MyProcessFunction())

    val processStream = sensorSource
      .keyBy(_.id)
      .process(new MyTimeIncreWarning(10 * 1000L))
    processStream.print("processStream")

    env.execute(this.getClass.getSimpleName)
  }


}

class MyTimeIncreWarning(interval: Long) extends KeyedProcessFunction[String, TempSensor, String] {
  // 定义状态，保存上一个温度值进行比较
  lazy val lastTempState: ValueState[Double] = getRuntimeContext.getState(new ValueStateDescriptor("lastTempState", classOf[Double]))
  // 定义状态，保存注册定时器的时间戳，用于删除定时器
  lazy val lastTimeState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor("lastTimeState", classOf[Long]))
  // lazy val valueSerialState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("lastTimeState", new Ser(classOf[Long])))

  override def processElement(value: TempSensor, ctx: KeyedProcessFunction[String, TempSensor, String]#Context, out: Collector[String]): Unit = {
    // 取出状态
    val lastTemp = lastTempState.value()
    val lastTime = lastTimeState.value()
    println(s"输入数据：$value 上一次的数据：$lastTemp ， $lastTime")
    // 判断当前温度和上一次温度比较
    lastTempState.update(value.temperature)
    if (value.temperature > lastTemp && lastTime == 0) { //温度上升并且没有定时器，则添加定时器
      ctx.timerService().registerProcessingTimeTimer(value.timestamp + interval)
      lastTimeState.update(value.timestamp)
    }
    if (value.temperature < lastTemp) { // 如果温度下降，删除定时器
      ctx.timerService().deleteProcessingTimeTimer(lastTime)
      lastTimeState.clear()
    }
  }

  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, TempSensor, String]#OnTimerContext, out: Collector[String]): Unit = {
    out.collect(s"传感器${ctx.getCurrentKey}的温度连续 ${interval / 1000} 秒上升")
    lastTimeState.clear()
  }
}


/**
 * KeyedProcessFunction功能测试
 * KeyedProcessFunction<K, I, O>  键值类型，输入类型，输出类型
 */
class MyProcessFunction extends KeyedProcessFunction[String, TempSensor, String] {

  var myState: ValueState[Int] = _

  override def open(parameters: Configuration): Unit = {
    myState = getRuntimeContext.getState(new ValueStateDescriptor[Int]("valueState", classOf[Int]))
  }

  override def processElement(value: TempSensor, ctx: KeyedProcessFunction[String, TempSensor, String]#Context, out: Collector[String]): Unit = {
    ctx.getCurrentKey //获取当前Key
    ctx.timestamp() //获取时间戳
    ctx.timerService().currentWatermark() //获取watermark
    ctx.timerService().registerEventTimeTimer(ctx.timestamp() + 10 * 1000L) //注册定时器，可以注册多个，都会条用onTimer方法
    // ctx.timerService().deleteEventTimeTimer(ctx.timestamp()) //删除定时器
  }

  /**
   * 定时器
   *
   * @param timestamp
   * @param ctx
   * @param out
   */
  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, TempSensor, String]#OnTimerContext, out: Collector[String]): Unit = {
    println("调用定时器：" + timestamp + " " + ctx.timestamp())
  }
}

