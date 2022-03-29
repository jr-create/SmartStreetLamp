package com.wjr.flink.stream.bean

import com.wjr.flink.stream.TempSensor
import org.apache.flink.api.common.functions.{FilterFunction, IterationRuntimeContext, RichMapFunction, RuntimeContext}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.SourceFunction

import scala.util.Random

/**
 * 自定义Source方法
 */
class MySourceFunction extends SourceFunction[TempSensor] {
    var running = true

    override def run(sourceContext: SourceFunction.SourceContext[TempSensor]): Unit = {
        while (running) {
            sourceContext.collect(TempSensor(s"device_00${Random.nextInt()}", System.currentTimeMillis(), Random.nextGaussian() * 20))
            Thread.sleep(100)
        }
    }

    override def cancel(): Unit = running = false
}

/**
 * 自定义Filter方法
 */
class MyFilter /*(value:String)*/ extends FilterFunction[TempSensor]{
    override def filter(t: TempSensor): Boolean = {
        t.temperature>1
    }
}
class MyRichMapper extends RichMapFunction[TempSensor,String]{
    override def setRuntimeContext(t: RuntimeContext): Unit = super.setRuntimeContext(t)

    override def getRuntimeContext: RuntimeContext = super.getRuntimeContext

    override def getIterationRuntimeContext: IterationRuntimeContext = super.getIterationRuntimeContext

    override def open(parameters: Configuration): Unit = super.open(parameters)

    override def close(): Unit = super.close()

    override def map(in: TempSensor): String = in.id+"rich"
}