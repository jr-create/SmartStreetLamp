package com.wjr.spark.sink

import com.wjr.spark.utils.MyPropertiesUtil
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}

import java.util.Properties
import java.util.concurrent.Future

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
  val broker_list: String = properties.getProperty("kafka.broker.list")
  //创建生产者对象
  var kafkaProducer: KafkaProducer[String, String] = null

  def createKafkaProducer: KafkaProducer[String, String] = {
    val properties = new Properties
    properties.put("bootstrap.servers", broker_list)
    properties.put("batch.size", "16384")
    properties.put("linger.ms", "1")
    properties.put("buffer.memory", "33554432")
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
    try {
      if (kafkaProducer == null) kafkaProducer = createKafkaProducer
      // 同步发送消息
      // kafkaProducer.send(new ProducerRecord[String, String](topic, msg))

      // 异步发送消息
      val asyncRecord = new ProducerRecord[String, String](topic, msg) //Topic Key Value
      val future: Future[RecordMetadata] = kafkaProducer.send(asyncRecord, new DemoProducerCallback()) //发送消息时，传递一个回调对象，该回调对象必须实现org.apache.kafka.clients.producer.Callback接口
      future.get()
    } catch {
      case exception: Exception => exception.printStackTrace()
    }
  }

  def send(topic: String, key: String, msg: String): Unit = {
    try {
      if (kafkaProducer == null) kafkaProducer = createKafkaProducer
      val future: Future[RecordMetadata] = kafkaProducer.send(new ProducerRecord[String, String](topic, key, msg), new DemoProducerCallback())
      future.get()
    } catch {
      case e => e.printStackTrace()
    }
  }
}

class DemoProducerCallback extends Callback {
  override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = {
    if (e != null) { //如果Kafka返回一个错误，onCompletion方法抛出一个non null异常。
      e.printStackTrace() //对异常进行一些处理，这里只是简单打印出来
    }
  }
}
