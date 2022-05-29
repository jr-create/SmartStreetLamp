package com.wjr.cdc.stream;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import com.wjr.cdc.bean.TempSource;
import com.wjr.cdc.uitls.MySourceUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.ListTypeInfo;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.stream
 * @ClassName: joinSource
 * @create 2022-05-13 11:53
 * @Description:
 */
public class joinSource {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("127.0.0.1")
                .port(3306)
                .databaseList("lamp") // 同步数据库中全部表
                .tableList("lamp.base_management") // set captured table
                .username("root")
                .password("123456")
                .startupOptions(StartupOptions.initial())
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        DataStreamSource<String> mySqlSourceDS = env
                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");
        // mySqlSourceDS.print().setParallelism(1);
        MapStateDescriptor<String, ArrayList<String>> mysqlStateDes = new MapStateDescriptor("MySqlBroadcast", BasicTypeInfo.STRING_TYPE_INFO, new ListTypeInfo<>(String.class));
        BroadcastStream<String> mySqlBroadcast = mySqlSourceDS.broadcast(mysqlStateDes);
        // KafkaSource
        DataStreamSource<String> kafkaSource = env.addSource(MySourceUtils.getKafkaConsumer("wjr"));
        kafkaSource.print("kafkaSource");
        SingleOutputStreamOperator<TempSource> kafkaMap = kafkaSource.map(value -> {
            String[] split = value.split(",");
            TempSource tempSource = new TempSource(split[0], split[1], System.currentTimeMillis());
            System.out.println("Kafka--" + tempSource);
            return tempSource;
        });
        // Join
        SingleOutputStreamOperator<Object> process = kafkaMap.connect(mySqlBroadcast)
                .process(new BroadcastProcessFunction<TempSource, String, Object>() {
                    @Override
                    public void processElement(TempSource value, BroadcastProcessFunction<TempSource, String, Object>.ReadOnlyContext ctx, Collector<Object> out) throws Exception {
                        System.out.println("processElement -- " + value);
                        ReadOnlyBroadcastState<String, ArrayList<String>> broadcastState = ctx.getBroadcastState(mysqlStateDes);
                        if (broadcastState.get("value") != null) {
                            broadcastState.get("value").forEach(broadValue -> {
                                JSONObject jsonObject = JSONObject.parseObject(broadValue);
                                if (jsonObject.getString("op").equals(value.getId())) {
                                    out.collect(broadValue);
                                }
                            });
                        }
                    }

                    @Override
                    public void processBroadcastElement(String value, BroadcastProcessFunction<TempSource, String, Object>.Context ctx, Collector<Object> out) throws Exception {
                        BroadcastState<String, ArrayList<String>> broadcastState = ctx.getBroadcastState(mysqlStateDes);
                        if (broadcastState.get("value") == null) {
                            ArrayList<String> list = new ArrayList<>();
                            list.add(value);
                            broadcastState.put("value", list);
                        } else {
                            ArrayList<String> list = broadcastState.get("value");
                            list.add(value);
                            broadcastState.put("value", list);
                        }
                        out.collect(value);
                    }
                });
        process.print("BroadcastProcessFunction");
        env.execute("Join");
    }
}
