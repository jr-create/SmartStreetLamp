package com.wjr.cdc.lamp;

import com.alibaba.fastjson.JSONObject;
import com.wjr.cdc.uitls.MySourceUtils;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.lamp
 * @ClassName: BaseLogApp
 * @create 2022-05-17 10:18
 * @Description:
 */
public class BaseLogApp {
    public static void main(String[] args) throws Exception {
        // TODO: 2022/5/17 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // TODO: 2022/5/17 消费Log日志，创建流
        DataStreamSource<String> KafkaDS = env.addSource(MySourceUtils.getKafkaConsumer("wjr"));

        // TODO: 2022/5/17 将数据流转为JSON对象
        SingleOutputStreamOperator<JSONObject> jsonDS = KafkaDS.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String value, ProcessFunction<String, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    System.out.println("解析异常");
                    e.printStackTrace();
                }
            }
        });
        // TODO: 2022/5/17 状态编程
        jsonDS.keyBy(jsonObj-> jsonObj.getJSONObject("value"));

        // TODO: 2022/5/17 分流
        // TODO: 2022/5/17 提取侧输出流
        // TODO: 2022/5/17 流输出
        // TODO: 2022/5/17 启动任务
        env.execute(BaseLogApp.class.getSimpleName());
    }
}
