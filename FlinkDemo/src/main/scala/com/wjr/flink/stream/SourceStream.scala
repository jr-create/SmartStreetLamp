package com.wjr.flink.stream


import com.wjr.flink.WordCount
import com.wjr.flink.stream.bean.MySourceFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.util.Properties

object SourceStream {
    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(2)

        val dataList = List(TempSensor("34", 123123123, 1.2), TempSensor("123", 123123123, 2.2))

        val sensors = env.fromCollection(dataList)
        val files = env.readTextFile(WordCount.path)
        files.print()
        sensors.print()
        // kafka数据源
        val properties = new Properties()
        properties.put("bootstrap.servers","localhost:9092")
        val kafkaDS: DataStream[String] = env.addSource(new FlinkKafkaConsumer[String]("wjr", new SimpleStringSchema(), properties))
        kafkaDS.print()
        // 自定义数据源
        val mySource = env.addSource(new MySourceFunction())
        mySource.writeAsText("/usr/local/IdeaWorkSpace/sparkDemo/FlinkDemo/src/main/resources/tempSensor.txt")
        mySource.print()

        env.execute("Source Stream")
    }

}

case class TempSensor(id: String, timestamp: Long, temperature: Double)