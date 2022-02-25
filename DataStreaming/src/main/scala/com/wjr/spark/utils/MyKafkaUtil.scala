package com.wjr.spark.utils

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

import java.util.Properties
import scala.collection.mutable

/**
 *
 * @Package: com.wjr.gmall.realtime.utils
 * @ClassName: MyKafkaUtil
 * @Author: 86157
 * @CreateTime: 2021/8/22 16:42
 * 从 Kafka 消费数据
 */
object MyKafkaUtil {

    private val properties : Properties = MyPropertiesUtil.load("config.properties")
    private val broker_list : String = properties.getProperty("kafka.broker.list")
    // kafka 消费者配置
    var kafkaParam = mutable.Map(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> broker_list,//用于初始化链接到集群的地址
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
        //用于标识这个消费者属于哪个消费团体
        ConsumerConfig.GROUP_ID_CONFIG -> "gmall_group",
        //latest 自动重置偏移量为最新的偏移量
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG  -> "latest",
        //如果是 true，则这个消费者的偏移量会在后台自动提交,但是 kafka 宕机容易丢失数据
        //如果是 false，会需要手动维护 kafka 偏移量
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

}
