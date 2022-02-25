package com.wjr.spark.utils

import com.alibaba.druid.pool.DruidDataSourceFactory
import com.wjr.spark.utils.decorator.JdbcSource

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.Properties
import javax.sql.DataSource

import com.wjr.spark.bean.DeviceCountInfo
import com.wjr.spark.streaming.SourceDataCountClassification.spark
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.SaveMode
import org.json4s.jackson.JsonMethods.compact

import java.sql.{Connection, DriverManager, ResultSet, ResultSetMetaData, Statement}
import scala.collection.mutable.ListBuffer

/**
 *
 * @Package: com.wjr.spark.sql.Utils
 * @ClassName: JDBCUtil
 * @Author: 86157
 * @CreateTime: 2021/7/31 19:05
 * @Description:
 */
object PhoenixJdbcUtil extends JdbcSource{

    def getConnection(): Connection = {
        super.getSource("phoenix").getConnection
    }

    /**
     * 查询 phoenix 工具类
     */
    //def queryList(sql: String): List[String] = {
    //    //注册驱动
    //    Class.forName("org.apache.phoenix.jdbc.PhoenixDriver")
    //    val resultList: ListBuffer[String] = new ListBuffer[String]()
    //    //建立连接
    //    val conn: Connection = DriverManager.getConnection("jdbc:phoenix:hadoop01,hadoop02,hadoop03:2181")
    //    //创建连接对象
    //    val stat: Statement = conn.createStatement
    //    //执行语句
    //    val rs: ResultSet = stat.executeQuery(sql)
    //    //处理元数据 结果
    //    val md: ResultSetMetaData = rs.getMetaData
    //    import org.json4s._
    //    implicit val formats = DefaultFormats
    //    while (rs.next) {
    //        var rowData = Map[Any, Any]()
    //        //{"user_id":"123","name":"123"}
    //        for (i <- 1 to md.getColumnCount) {
    //            //获取每个列的数据，并转换成json
    //            rowData += md.getColumnName(i) -> rs.getObject(i)
    //        }
    //        resultList += compact(Extraction.decompose(rowData))
    //    }
    //    stat.close()
    //    conn.close()
    //    resultList.toList
    //}
    def main(args: Array[String]): Unit = {

        val connection = PhoenixJdbcUtil.getConnection
        val list : List[String] = queryList(connection,"select * from  DeviceCountInfo")

        val bool =PhoenixJdbcUtil.executeSql(connection,SqlUtils.insertPhoenixData("DeviceCountInfo",
            JsonUtils.getObjectValue(DeviceCountInfo(5,345,345,564,123,123,123,"2022-02-04 16:00:00"))))
        //val bool = JDBCUtil.executeSql(connection,
        //    """
        //      |upsert into DEVICECOUNTINFO Values(3,123,123,123,123,123,123,'2022-02-04 11:00:00')
        //      |""".stripMargin)
        println(bool)
        connection.close()
        println(list)
    }

}
