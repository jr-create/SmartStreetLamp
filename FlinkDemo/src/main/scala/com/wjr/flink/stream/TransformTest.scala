package com.wjr.flink.stream

import com.wjr.flink.WordCount
import org.apache.flink.streaming.api.scala._

import scala.util.Random

object TransformTest {
    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val inputStream = env.readTextFile(WordCount.path)
        val dataStream = inputStream.map(data => {
            val values = data.split(" ")
            TempSensor(values(0), System.currentTimeMillis(), Random.nextGaussian() + 20)
        })
        // 分组聚合
        val aggstream = dataStream.keyBy(_.id).sum("temperature")
        //aggstream.print()

        val reduceStream = dataStream.keyBy("id").reduce((curState, newDate) => {
            TempSensor(curState.id, newDate.timestamp, curState.temperature.min(newDate.temperature))
        })
        //reduceStream.print()
        //分流三种方式
        /**
         * 方式一
         * Fliter分流
         */
        dataStream.filter(_.temperature > 19)

        /**
         * 方式二
         * Split分流 1.14 DataStream不具备
         */
        /**
         * 方式三
         * Side-outputs分流(推荐使用)
         * 步骤：
         * 定义 OutputTag
         * process 算子中收集数据
         * ProcessFunction
         * KeyedProcessFunction
         * CoProcessFunction
         * ProcessWindowFunction
         * ProcessAllWindowFunction
         *
         */
        val outTag1 = OutputTag[TempSensor]("notIsOne")
        val outTag2 = OutputTag[TempSensor]("IsOne")
        val outputStream = dataStream.process[TempSensor]((sensor, context, collection) => {
            if (sensor.temperature > 20) {
                context.output(outTag1, sensor)
            } else {
                context.output(outTag2, sensor)
            }
            println(collection)
        })
        val highStream = outputStream.getSideOutput(outTag1)
        val slowStream = outputStream.getSideOutput(outTag2)
        highStream.connect(slowStream)
        //highStream.map((_, "highStream")).print("high")
        //slowStream.map((_, "slowStream")).print("slow")

        /**
         * 合流
         * 应用场景：
         *  将温度传感器数据和烟雾传感器数据进行合流
         *  只有 当温度传感器和烟雾传感器的数据 都超过阈值时，才进行报警。
         */
        val warnStream = highStream.map(x => (x.id, x.temperature))
        val connectStream: ConnectedStreams[(String, Double), TempSensor] = warnStream.connect(slowStream)

        // coMap对流数据处理
        val coMapResult :DataStream[Any] = connectStream.map(
            warnData => (warnData._1, warnData._2, "warning"),
            slowData => (slowData.id, "healthy")
        )
        coMapResult.print("connectStreamAndCoMap")

        val unionStream = highStream.union(slowStream)
        unionStream.print("unionStream")
        env.execute("Transform Data")
    }

}
