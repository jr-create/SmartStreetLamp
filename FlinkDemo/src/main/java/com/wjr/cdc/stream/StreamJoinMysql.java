package com.wjr.cdc.stream;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.$;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.stream
 * @ClassName: StreamJoinTest
 * @create 2022-05-13 20:04
 * @Description:
 */
public class StreamJoinMysql {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(5);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        tableEnv.executeSql("CREATE TABLE flink_management (\n" +
                "  id int NOT NULL ,\n" +
                "  name STRING ,\n" +
                "  sex STRING,\n" +
                "  age int ,\n" +
                "  phone STRING,\n" +
                "  email STRING ,\n" +
                "  card_id STRING ,\n" +
                "  road_name STRING ,\n" +
                "  start_time TIMESTAMP(3),\n" +
                "  end_time TIMESTAMP(3),\n" +
                "  PRIMARY KEY(id) NOT ENFORCED \n" + //需要加上primary key
                ")  WITH (\n" +
                " 'connector' = 'mysql-cdc',\n" +
                " 'hostname' = 'localhost',\n" +
                " 'port' = '3306',\n" +
                " 'username' = 'root',\n" +
                " 'password' = '123456',\n" +
                " 'database-name' = 'lamp',\n" +
                " 'table-name' = 'base_management'\n" +
                ")\n");
        String kafkaSourceSql = "CREATE TABLE kafkaTable (\n" +
                        "  json STRING\n" +
                        ") WITH (\n" +
                        "  'connector' = 'kafka',\n" +
                        "  'topic' = 'wjr',\n" +
                        "  'properties.bootstrap.servers' = '124.220.2.188:9092',\n" +
                        "  'properties.group.id' = 'testGroup',\n" +
                        "  'scan.startup.mode' = 'latest-offset',\n" +
                        "  'format' = 'raw')";
        tableEnv.executeSql(kafkaSourceSql);
        Table kafkaTable = tableEnv.from("kafkaTable");
        Table mysql_management = tableEnv.from("mysql_management").select($("name"));
        Table joinS = kafkaTable.join(mysql_management).where($("name").isEqual($("json")));
        // joinS.execute().print();
        DataStream<Row> rowDataStream = tableEnv.toChangelogStream(joinS);
        DataStream<Tuple2<Boolean, Row>> tuple2DataStream = tableEnv.toRetractStream(joinS, Row.class);
        // tuple2DataStream.print("toRetractStream");
        // rowDataStream.executeAndCollect().forEachRemaining(System.out::println);
        // rowDataStream.print("toChangelogStream");
        env.execute("stream join Mysql" );
    }
}
