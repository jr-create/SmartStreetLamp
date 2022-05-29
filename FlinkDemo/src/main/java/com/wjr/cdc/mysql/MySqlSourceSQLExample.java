package com.wjr.cdc.mysql;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc
 * @ClassName: MySqlSourceSQLExample
 * @create 2022-05-01 17:04
 * @Description:
 */
public class MySqlSourceSQLExample {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
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
        TableResult tableResult = tableEnv.executeSql("select * from flink_management");
        tableResult.print();

    }
}


