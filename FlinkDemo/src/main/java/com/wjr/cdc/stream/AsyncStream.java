package com.wjr.cdc.stream;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import com.wjr.cdc.bean.TempSource;
import com.wjr.cdc.uitls.MySourceUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.OutputTag;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.stream
 * @ClassName: AsyncStream
 * @create 2022-05-13 19:27
 * @Description:
 */
public class AsyncStream {
    public static StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    public static void main(String[] args) throws Exception {

        // Kafka
        SingleOutputStreamOperator<TempSource> tempTest2 = env.addSource(MySourceUtils.getKafkaConsumer("wjr"))
                .map(value -> {
                    String[] split = value.split(",");
                    TempSource tempSource = new TempSource(split[0], split[1], System.currentTimeMillis());
                    System.out.println("Kafka--" + tempSource);
                    return tempSource;
                });
        OutputTag<String> outputTag = new OutputTag<String>("side-output") {};
        SingleOutputStreamOperator<Tuple2<String, String>> asyncStream = AsyncDataStream
                .orderedWait(tempTest2,
                        new AsyncDatabaseRequest(),
                        100000L,
                        TimeUnit.MILLISECONDS,
                        20);
        asyncStream.print("asyncStream");
        // Interval Join
        // SingleOutputStreamOperator<TempSource> stream1 = tempTest1.assignTimestampsAndWatermarks(WatermarkStrategy.<TempSource>forMonotonousTimestamps()
        //         .withTimestampAssigner(new SerializableTimestampAssigner<TempSource>() {
        //             @Override
        //             public long extractTimestamp(TempSource element, long recordTimestamp) {
        //                 return element.getTimestamp();
        //             }
        //         }));
        //
        // SingleOutputStreamOperator<TempSource> stream2 = tempTest2.assignTimestampsAndWatermarks(WatermarkStrategy.<TempSource>forMonotonousTimestamps()
        //         .withTimestampAssigner(new SerializableTimestampAssigner<TempSource>() {
        //             @Override
        //             public long extractTimestamp(TempSource element, long recordTimestamp) {
        //                 return element.getTimestamp();
        //             }
        //         }));
        // SingleOutputStreamOperator<Object> joinDS = stream1.keyBy(value -> value.getId())
        //         .intervalJoin(stream2.keyBy(value -> value.getId()))
        //         .between(Time.seconds(-2), Time.seconds(1))
        //         // .lowerBoundExclusive() 表示(] 同时不存在表示[]
        //         // .upperBoundExclusive() 表示[) 同时存在表示()
        //         .process(new ProcessJoinFunction<TempSource, TempSource, Object>() {
        //             @Override
        //             public void processElement(TempSource left, TempSource right, ProcessJoinFunction<TempSource, TempSource, Object>.Context ctx, Collector<Object> out) throws Exception {
        //                 System.out.println(left + "  " + right);
        //                 out.collect(new Tuple2<>(left, right));
        //             }
        //         });
        // joinDS.print("joinDS");


        env.execute("AsyncStream TEST");
    }
}

class AsyncDatabaseRequest extends RichAsyncFunction<TempSource, Tuple2<String, String>> {
    MySqlSource<String> mySqlSource;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        mySqlSource = MySqlSource.<String>builder()
                .hostname("127.0.0.1")
                .port(3306)
                .databaseList("lamp") // set captured database, If you need to synchronize the whole database, Please set tableList to ".*".
                .tableList("lamp.base_management") // set captured table
                .username("root")
                .password("123456")
                .startupOptions(StartupOptions.initial())// 在第一次启动时对受监控的数据库表执行初始快照，并继续读取最新的binlog。
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                // .deserializer(new WithCustomerDeserialization()) // 自定义String->JSON
                .build();

    }


    @Override
    public void asyncInvoke(TempSource tempSource, ResultFuture<Tuple2<String, String>> resultFuture) throws Exception {
        SingleOutputStreamOperator<TempSource> mySqlSourceDS = AsyncStream.env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
                .map(value -> {
                    System.out.println("MySQL Source -- " + value);
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    return new TempSource(jsonObject.getString("op"), jsonObject.getString("op"), System.currentTimeMillis());
                });
        SingleOutputStreamOperator<TempSource> filter = mySqlSourceDS.filter(value -> value.getId() == tempSource.getId());
        ArrayList<Tuple2<String, String>> tempSources = new ArrayList<>();
        filter.map(value -> {
            System.out.println(value);
            tempSources.add(new Tuple2<>(value.getId(), value.getName()));
            return value;
        });
        System.out.println("asyncInvoke -- "+tempSources);
        resultFuture.complete(tempSources);
        // resultFuture.complete(Collections.singleton(new Tuple2<>(tempSource.getId(), tempSource.getName())));
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

}
