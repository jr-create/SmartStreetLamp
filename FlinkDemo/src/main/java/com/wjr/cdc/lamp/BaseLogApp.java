package com.wjr.cdc.lamp;

import akka.japi.tuple.Tuple3;
import com.wjr.cdc.bean.SmartLampSource;
import com.wjr.cdc.uitls.BeanUtils;
import com.wjr.cdc.uitls.MySourceUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
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
        DataStreamSource<String> KafkaDS = env.addSource(MySourceUtils.getKafkaConsumer("ods_lamp_log"));

        // TODO: 2022/5/17 将数据流转为JSON对象
        SingleOutputStreamOperator<SmartLampSource> smartDS = KafkaDS.process(new ProcessFunction<String, SmartLampSource>() {

            @Override
            public void processElement(String value, ProcessFunction<String, SmartLampSource>.Context ctx, Collector<SmartLampSource> out) {
                try {
                    SmartLampSource object = BeanUtils.jsonToSmart(value);
                    out.collect(object);
                } catch (Exception e) {
                    System.out.println("解析异常");
                    e.printStackTrace();
                }
            }
        });
        // // TODO: 2022/5/17 状态编程
        smartDS.keyBy(value -> value.getFields().get("device_id"))
                .flatMap(new TempChangeAlertFlatMap());
        smartDS.map(value -> value.getFields().get("reason")).print("12313");
        // TODO: 2022/5/17 分流
        // TODO: 2022/5/17 提取侧输出流
        // TODO: 2022/5/17 流输出
        // TODO: 2022/5/17 启动任务
        env.execute(BaseLogApp.class.getSimpleName());
    }
}

class TempChangeAlertFlatMap extends RichFlatMapFunction<SmartLampSource, Tuple3<String, Double, Double>> {

    public TempChangeAlertFlatMap() {
    }
    Double threshold;
    public TempChangeAlertFlatMap(Double threshold) {
        this.threshold = threshold;
    }

    //定义状态，保存上一个温度值
    ValueState<Double> lastTempState;

    @Override
    public RuntimeContext getRuntimeContext() {
        lastTempState = getRuntimeContext().getState(new ValueStateDescriptor<>("temperaState", Double.class));
        return super.getRuntimeContext();
    }


    @Override
    public void flatMap(SmartLampSource value, Collector<Tuple3<String, Double, Double>> out) throws Exception {
        Double lastTemp = lastTempState.value(); //获取上一次的值
        // 与最新温度值进行比较
        Double values_temperature = value.<Double>getValue("values_temperature");
        String device_id = value.getValue("device_id");
        Double diffTemp = Math.abs(values_temperature - lastTemp);
        if (diffTemp >= threshold) {
            // 输出结果
            out.collect(new Tuple3(device_id, lastTemp, values_temperature));
            // 更新状态
            lastTempState.update(values_temperature);
        }
    }
}
