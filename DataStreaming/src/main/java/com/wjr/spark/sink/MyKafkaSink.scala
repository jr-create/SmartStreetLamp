package com.wjr.spark.sink

import com.wjr.spark.utils.MyPropertiesUtil
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import java.util.Properties

/**
 *
 * @Package: com.wjr.gmall.realtime.utils
 * @ClassName: MyKafkaSink
 * @Author: 86157
 * @CreateTime: 2021/8/26 18:36
 *              向 Kafka 中写入数据工具类
 */
object MyKafkaSink {

    //获取配置文件信息
    private val properties: Properties = MyPropertiesUtil.load("config.properties")
    //获取kafka集群
    val broker_list = properties.getProperty("kafka.broker.list")
    //创建生产者对象
    var kafkaProducer: KafkaProducer[String, String] = null

    def createKafkaProducer: KafkaProducer[String, String] = {
        val properties = new Properties
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker_list)
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        properties.put("enable.idempotence", (true: java.lang.Boolean))
        var producer: KafkaProducer[String, String] = null
        try {
            producer = new KafkaProducer[String, String](properties)
        } catch {
            case e: Exception =>
                e.printStackTrace()
        }
        producer
    }

    def send(topic: String, msg: String): Unit = {
        if (kafkaProducer == null) kafkaProducer = createKafkaProducer
        kafkaProducer.send(new ProducerRecord[String, String](topic, msg))
    }

    def send(topic: String, key: String, msg: String): Unit = {
        if (kafkaProducer == null) kafkaProducer = createKafkaProducer
        kafkaProducer.send(new ProducerRecord[String, String](topic, key, msg))

    }

}
