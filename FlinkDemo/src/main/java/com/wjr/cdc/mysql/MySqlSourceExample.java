package com.wjr.cdc.mysql;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc
 * @ClassName: MySqlSourceExample
 * @create 2022-05-01 12:42
 * @Description: 连接Mysql案例
 */
public class MySqlSourceExample {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //3.创建 Flink-MySQL-CDC 的 Source
        //initial (default): Performs an initial snapshot on the monitored database tables upon first startup, and continue to read the latest binlog.
        //latest-offset: Never to perform snapshot on the monitored database tables upon first startup, just read from the end of the binlog which means only have the changes since the connector was started.
        //timestamp: Never to perform snapshot on the monitored database tables upon first startup, and directly read binlog from the specified timestamp. The consumer will traverse the binlog from the beginning and ignore change events whose timestamp is smaller than the specified timestamp.
        //specific-offset: Never to perform snapshot on the monitored database tables upon first startup, and directly read binlog from the specified offset.
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
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

        DataStreamSource<String> mySqlSourceDS = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");
        mySqlSourceDS.print("sqlSource");
        env.execute("Print MySQL Snapshot + Binlog");
    }
}
