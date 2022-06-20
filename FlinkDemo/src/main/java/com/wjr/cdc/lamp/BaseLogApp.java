package com.wjr.cdc.lamp;

import com.alibaba.fastjson.JSONObject;
import com.wjr.cdc.constant.ExceptionConstant;
import com.wjr.cdc.uitls.MySourceUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

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
        DataStreamSource<String> KafkaDS = env.addSource(MySourceUtils.getKafkaConsumer("ods_lamp_log"));

        // 异常输出流
        OutputTag<Tuple2<Integer, String>> outputTag = new OutputTag<>("Dirty");

        // TODO: 2022/5/17 将数据流转为JSON对象
        SingleOutputStreamOperator<JSONObject> jsonObjectDS = KafkaDS.process(new ProcessFunction<String, JSONObject>() {

            @Override
            public void processElement(String value, ProcessFunction<String, JSONObject>.Context ctx, Collector<JSONObject> out) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    // 发生异常，将数据发送到侧输出流中
                    ctx.output(outputTag, new Tuple2<>(ExceptionConstant.BASE_EXCEPTION, value));
                    e.printStackTrace();
                }
            }
        });
        // TODO: 2022/6/20 类型分类，
        jsonObjectDS.keyBy(value -> value.getString("type_id"))
                .map(new RichMapFunction<JSONObject, JSONObject>() {

                    //定义状态，保存上一个温度值
                    ValueState<String> valueState;

                    // 状态初始化
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        valueState = getRuntimeContext().getState(new ValueStateDescriptor<>("valueState", String.class));
                    }

                    @Override
                    public JSONObject map(JSONObject value) throws Exception {
                        Integer error_code = value.getInteger("error_code");
                        // 判断设备异常码是否为1
                        if (error_code == 1) {
                            String state = valueState.value();
                            if (null == state) {
                                valueState.update("1");
                            }
                            return null;
                        }else{
                            return value;
                        }
                    }
                });
        // TODO: 2022/5/17 分流
        // TODO: 2022/5/17 提取侧输出流
        // TODO: 2022/5/17 流输出
        // TODO: 2022/5/17 启动任务
        env.execute(BaseLogApp.class.getSimpleName());
    }
}

