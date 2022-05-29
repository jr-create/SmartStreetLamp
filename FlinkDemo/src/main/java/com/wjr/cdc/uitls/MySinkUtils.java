package com.wjr.cdc.uitls;

import com.wjr.cdc.constant.MyAnyProperties;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.uitls
 * @ClassName: SinkDataStream
 * @create 2022-05-13 17:17
 * @Description:
 */
public class MySinkUtils {

    public static FlinkKafkaProducer<String> getKafkaProducer(String topic) {
        return new FlinkKafkaProducer<String>(topic, new SimpleStringSchema(), MyAnyProperties.getKafkaProperties());
    }

    public static FlinkKafkaProducer<String> getKafkaProducer(String brokers, String topic, String groupId) {
        return new FlinkKafkaProducer<>(topic, new SimpleStringSchema(), MyAnyProperties.getKafkaProperties(brokers, groupId));
    }

    public static FlinkKafkaProducer<String> getKafkaProducer(String topic, Properties properties) {
        return new FlinkKafkaProducer<>(topic, new SimpleStringSchema(), properties);
    }
}
