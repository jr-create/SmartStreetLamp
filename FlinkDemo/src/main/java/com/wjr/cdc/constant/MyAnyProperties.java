package com.wjr.cdc.constant;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.constant
 * @ClassName: MyAnyProperties
 * @create 2022-05-13 17:11
 * @Description:
 */
public class MyAnyProperties {
    public static Properties getKafkaProperties(){
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "124.220.2.188:9092");
        return properties;
    }

    public static Properties getKafkaProperties(String brokers,String groupId){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

        return properties;
    }
}
