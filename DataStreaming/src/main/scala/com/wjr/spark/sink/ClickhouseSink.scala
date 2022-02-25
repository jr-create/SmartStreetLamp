package com.wjr.spark.sink

import com.wjr.spark.bean.DeviceCountInfo
import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.utils.{ClickhouseJdbcUtil, JsonUtils, SqlUtils}
import org.apache.spark
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}

import java.util.Properties
import scala.collection.JavaConverters.mapAsJavaMap
import scala.collection.{generic, mutable}
import scala.util.parsing.json.JSON.headOptionTailToFunList

/**
 * @Package: com.wjr.spark.sink
 * @ClassName: ClickhouseSink
 * @author Lenovo-wjr
 * @create 2022-02-03 21:19
 * @Description:
 */
object ClickhouseSink {

    val propertiesMap = mutable.Map[String, String]()
    propertiesMap.put("driver", "ru.yandex.clickhouse.ClickHouseDriver")
    propertiesMap.put("user", "wjr")
    propertiesMap.put("password", "v724FzVr")
    propertiesMap.put("socket_timeout", "300000")
    propertiesMap.put("numPartitions", "1") // 设置并发
    propertiesMap.put("rewriteBatchedStatements", "true")
    propertiesMap.put("isolationLevel", "NONE")
    propertiesMap.put("url", "jdbc:clickhouse://hadoop01:8123/default")
    propertiesMap.put("rewriteBatchedStatements","true")
    val prop = new Properties()
    prop.putAll(mapAsJavaMap(propertiesMap.toMap))

    /**
     * 无法在流数据中使用
     * @param tbName
     * @param data
     * @param dbName
     */
    def DeviceCountInfoExport(tbName: String, data: mutable.Seq[DeviceCountInfo], dbName: String = "default"): Unit = {
        ProjectEnv.spark.createDataFrame(data).write.format("jdbc").mode(SaveMode.Append)
            .options(propertiesMap)
            .option(JDBCOptions.JDBC_BATCH_INSERT_SIZE, 100000)
            .option("dbtable", tbName)
            .save()
    }

    def main(args: Array[String]) {

        //val manager = DwsRoadManager(1, 1, 1, 1, 1, 1, 1, "2022-02-03 11:00:01", "", "", "", "", "", "")
        //DeviceCountInfoExport("dwd_device_count", mutable.Seq(DeviceCountInfo(1, 1L, 1L, 1L, 1L, 1L, 1L, "2022-02-03 11:00:01")))
        val connection = ClickhouseJdbcUtil.getConnection()
        //val array = JsonUtils.getObjectValue(DeviceCountInfo(1, 1L, 1L, 1L, 1L, 1L, 1L, "2022-02-03 13:00:01"))
        //val bool = ClickhouseJdbcUtil
        //    .executeSql(connection,SqlUtils.insertSql("dwd_device_count",array.toBuffer))
        //println(bool)
        val list: List[String] = ClickhouseJdbcUtil.queryList(connection, "select * from dwd_device_count")
        println(list)
    }

}
