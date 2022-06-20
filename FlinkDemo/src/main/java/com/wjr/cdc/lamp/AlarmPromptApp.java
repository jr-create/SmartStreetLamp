package com.wjr.cdc.lamp;

import akka.japi.tuple.Tuple3;
import com.wjr.cdc.bean.SmartLampSource;
import com.wjr.cdc.constant.ExceptionConstant;
import com.wjr.cdc.uitls.BeanUtils;
import com.wjr.cdc.uitls.MySourceUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
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
 * @ClassName: AlarmPrompt
 * @create 2022-06-06 15:17
 * @Description: 温度数据报警提示
 */
public class AlarmPromptApp {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // TODO: 2022/5/17 消费Log日志，创建流
        // TODO: 2022/5/17 消费Log日志，创建流
        DataStreamSource<String> KafkaDS = env.addSource(MySourceUtils.getKafkaConsumer("ods_lamp_log"));

        // 异常输出流
        OutputTag<Tuple2<Integer,String>> outputTag = new OutputTag<>("Dirty");

        // 将数据流转为SmartLampSource对象
        SingleOutputStreamOperator<SmartLampSource> smartDS = KafkaDS.process(new ProcessFunction<String, SmartLampSource>() {

            @Override
            public void processElement(String value, ProcessFunction<String, SmartLampSource>.Context ctx, Collector<SmartLampSource> out) {
                try {
                    SmartLampSource object = BeanUtils.jsonToSmart(value);
                    out.collect(object);
                } catch (Exception e) {
                    // 发生异常，将数据发送到侧输出流中
                    ctx.output(outputTag,new Tuple2<>(ExceptionConstant.BASE_EXCEPTION,value));
                    e.printStackTrace();
                }
            }
        });
        /**
         * 需求：温度值突然跳变10度，需要报警
         */
        smartDS.keyBy(value -> value.getFields().get("type_id"))
                .flatMap(new TempChangeAlertFlatMap());

        smartDS.map(value -> value.getFields().get("reason")).print("12313");


        env.execute("AlarmPromptApp");
    }
}

class TempChangeAlertFlatMap extends RichFlatMapFunction<SmartLampSource, Tuple3<String, Double, Double>> {

    public TempChangeAlertFlatMap() {}

    Double threshold;

    public TempChangeAlertFlatMap(Double threshold) {
        this.threshold = threshold;
    }

    //定义状态，保存上一个温度值
    ValueState<Double> lastTempState;

    /**
     * 状态初始化
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        lastTempState = getRuntimeContext().getState(new ValueStateDescriptor<>("temperaState", Double.class));
    }


    @Override
    public void flatMap(SmartLampSource value, Collector<Tuple3<String, Double, Double>> out) throws Exception {
        //获取上一次的值，默认为0
        Double lastTemp = lastTempState.value();
        // 最新温度值
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
