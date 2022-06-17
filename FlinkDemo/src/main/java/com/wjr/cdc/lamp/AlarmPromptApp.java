package com.wjr.cdc.lamp;

import com.alibaba.fastjson.JSONObject;
import com.wjr.cdc.uitls.MySourceUtils;
import com.wjr.lamp.utils.JsonUtils;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import scala.collection.immutable.List;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.lamp
 * @ClassName: AlarmPrompt
 * @create 2022-06-06 15:17
 * @Description:
 */
public class AlarmPromptApp {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // TODO: 2022/5/17 消费Log日志，创建流
        DataStreamSource<String> kafkaSource = env.addSource(MySourceUtils.getKafkaConsumer("lamp_ods"));

        SingleOutputStreamOperator<JSONObject> lampValues = kafkaSource.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String value, ProcessFunction<String, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    List<Object> objectList = JsonUtils.jsonToList(value);
                    System.out.println(objectList);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    System.out.println("解析异常");
                    e.printStackTrace();
                }
            }
        });
        /**
         * 需求：温度值突然跳变10度，需要报警
         */
        lampValues.print("lampValue");
        env.execute("");
    }
}
