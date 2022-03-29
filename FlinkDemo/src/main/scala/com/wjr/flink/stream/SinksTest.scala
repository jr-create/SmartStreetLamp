package com.wjr.flink.stream

import org.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.{compact}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties
import scala.util.Random
object SinksTest {


    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment

        env.setParallelism(1)

        // kafka数据源
        val properties = new Properties()
        properties.put("bootstrap.servers", "localhost:9092")
        properties.setProperty("group.id", "test")
        val myConsumer = new FlinkKafkaConsumer[String]("wjr", new SimpleStringSchema(), properties)
        //myConsumer.assignTimestampsAndWatermarks(
        //        WatermarkStrategy
        //            .forBoundedOutOfOrderness(Duration.ofSeconds(20)))
        val inputStream: DataStream[String] = env.addSource(myConsumer)
        inputStream.print()


        val dataStream = inputStream.map(data => {
            implicit val formats = DefaultFormats
            val values = data.split(" ")
            val sensor = TempSensor(values(0), System.currentTimeMillis(), Random.nextGaussian() + 20)
            val str = compact(Extraction.decompose(sensor))
            println("输入数据："+str)
            str
        })

        //dataStream.writeAsText("out1.txt")
        //dataStream.addSink(
        //    StreamingFileSink.forRowFormat(new Path("out2.txt"),
        //        new SimpleStringEncoder[String]()).build())


        // KafkaSink 只能String类型
        dataStream.addSink(
            new FlinkKafkaProducer[String](
                "localhost:9092",
                "wjr2", // 目标 topic
                new SimpleStringSchema()))

        // RedisSink
        // val conf = new FlinkJedisPoolConfig.Builder().setHost("127.0.0.1").build()
        // dataStream.addSink(new RedisSink[String](conf, new RedisSinkFunc))


        // val conf = new FlinkJedisClusterConfig.Builder().setNodes(...).build()
        // dataStream.addSink(new RedisSink[(String, String)](conf, new RedisExampleMapper))

        // val conf = new FlinkJedisSentinelConfig.Builder().setMasterName("master").setSentinels(...).build()
        // dataStream.addSink(new RedisSink[(String, String)](conf, new RedisExampleMapper))

        // JDBCSink
        dataStream.addSink(new MysqlSinkFunc())
        env.execute("Sink Stream")
    }

}

class RedisSinkFunc extends RedisMapper[String] {
    override def getCommandDescription: RedisCommandDescription = {
        new RedisCommandDescription(RedisCommand.HSET, "HASH_NAME")
    }

    override def getKeyFromData(t: String): String = t.split("\\(").last.charAt(0).toString

    override def getValueFromData(t: String): String = t
}

class MysqlSinkFunc extends RichSinkFunction[String] {
    var conn: Connection = _
    var insertStmt: PreparedStatement = _
    var updateStmt: PreparedStatement = _
    override def open(parameters: Configuration): Unit = {
        super.open(parameters)
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "Ren@0072")
        insertStmt = conn.prepareStatement("INSERT INTO temperatures (temp, timestamp) VALUES (?, ?)")
        updateStmt = conn.prepareStatement("UPDATE temperatures SET temp = ? , timestamp = ? WHERE id = ?")
    }

    override def invoke(in: String): Unit = {
        println("接收数据："+in)
        implicit val formats = DefaultFormats

        val value = JsonMethods.parse(in).extract[TempSensor]
        println("转换类型："+value.toString)
        updateStmt.setDouble(1, value.temperature)
        updateStmt.setDouble(2, value.timestamp)
        updateStmt.setString(3, value.id)
        updateStmt.execute()
        if (updateStmt.getUpdateCount == 0) {
            insertStmt.setDouble(1, value.temperature)
            insertStmt.setDouble(2, value.timestamp)
            insertStmt.execute()
        }
    }

    override def close(): Unit = {
        insertStmt.close()
        updateStmt.close()
        conn.close()
    }
}