package com.wjr.cdc.uitls;

import com.wjr.cdc.constant.MyAnyProperties;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.uitls
 * @ClassName: SourceDataStream
 * @create 2022-05-13 11:05
 * @Description:
 */
public class MySourceUtils {
    /**
     * kafka数据源
     */
    public static FlinkKafkaConsumer<String> getKafkaConsumer(String topic) {
        return new FlinkKafkaConsumer<>(topic, new SimpleStringSchema(), MyAnyProperties.getKafkaProperties());
    }

    public static FlinkKafkaConsumer<String> getKafkaConsumer(String brokerList, String topic, String groupId) {
        return new FlinkKafkaConsumer<>(topic, new SimpleStringSchema(), MyAnyProperties.getKafkaProperties(brokerList, groupId));
    }

    public static FlinkKafkaConsumer<String> getKafkaConsumer(String topic, Properties properties) {
        return new FlinkKafkaConsumer<>(topic, new SimpleStringSchema(), properties);
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        FlinkKafkaConsumer<String> kafkaSource = MySourceUtils.getKafkaConsumer("wjr");
        DataStreamSource<String> stringDataStreamSource = env.addSource(kafkaSource);
        stringDataStreamSource.print("tempSource");


        env.execute("Source TEST");
    }
}
