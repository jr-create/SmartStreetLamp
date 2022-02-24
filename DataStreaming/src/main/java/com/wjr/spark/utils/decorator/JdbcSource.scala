package com.wjr.spark.utils.decorator

import com.alibaba.druid.pool.DruidDataSourceFactory
import com.wjr.spark.utils.MyPropertiesUtil
import org.json4s.jackson.JsonMethods.compact

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, ResultSetMetaData, Statement}
import java.util.Properties
import javax.sql.DataSource
import scala.collection.mutable.ListBuffer

/**
 * @Package: com.wjr.spark.utils.decorator
 * @ClassName: JDBC
 * @author Lenovo-wjr
 * @create 2022-02-06 16:54
 * @Description:
 */
trait JdbcSource {
    //初始化连接池
    var dataSource: DataSource = _

    //初始化连接池方法
    def init(sourceName: String): DataSource = {
        val properties = new Properties()
        val config: Properties = MyPropertiesUtil.load("config.properties")
        properties.setProperty("driverClassName", config.getProperty(s"$sourceName.driver"))
        properties.setProperty("url", config.getProperty(s"$sourceName.url"))
        properties.setProperty("username", config.getProperty(s"$sourceName.user"))
        properties.setProperty("password", config.getProperty(s"$sourceName.password"))
        properties.setProperty("maxActive", config.getProperty("jdbc.datasource.size"))
        properties.setProperty("validationQuery", "SELECT 1")
        DruidDataSourceFactory.createDataSource(properties)
    }

    //获取 连接
    def getSource(sourceName: String): DataSource = {
        dataSource = init(sourceName)
        dataSource
    }


    //执行 SQL 语句,单条数据插入
    def executeUpdate(connection: Connection, sql: String, params: Array[Any]): Int = {
        var rtn = 0
        var pstmt: PreparedStatement = null
        try {
            connection.setAutoCommit(false)
            pstmt = connection.prepareStatement(sql)

            if (params != null && params.length > 0) {
                for (i <- params.indices) {
                    pstmt.setObject(i + 1, params(i))
                }
            }
            rtn = pstmt.executeUpdate()
            connection.commit()
            pstmt.close()
        } catch {
            case e: Exception => e.printStackTrace()
        }
        rtn
    }

    //执行 SQL 语句,批量数据插入
    def executeBatchUpdate(connection: Connection, sql: String, paramsList: Iterable[Array[Any]]): Array[Int] = {
        var rtn: Array[Int] = null
        var pstmt: PreparedStatement = null
        try {
            connection.setAutoCommit(false) // TODO: 关闭自动提交
            pstmt = connection.prepareStatement(sql)
            for (params <- paramsList) {
                if (params != null && params.length > 0) {
                    for (i <- params.indices) {
                        pstmt.setObject(i + 1, params(i))
                    }
                    pstmt.addBatch()
                }
            }
            rtn = pstmt.executeBatch()
            connection.commit()
            pstmt.close()
        }
        catch {
            case e: Exception => e.printStackTrace()
        }
        rtn
    }

    //判断一条数据是否存在
    def isExist(connection: Connection, sql: String, params: Array[Any]): Boolean = {
        var flag: Boolean = false
        var pstmt: PreparedStatement = null
        try {
            pstmt = connection.prepareStatement(sql)
            for (i <- params.indices) {
                pstmt.setObject(i + 1, params(i))
            }
            flag = pstmt.executeQuery().next()
            pstmt.close()
        } catch {
            case e: Exception => e.printStackTrace()
        }
        flag
    }

    //获取一条数据
    def getDataFromMysql(connection: Connection, sql: String, params: Array[Any]): Long = {
        var result: Long = 0L
        var pstmt: PreparedStatement = null
        try {
            pstmt = connection.prepareStatement(sql)
            for (i <- params.indices) {
                pstmt.setObject(i + 1, params(i))
            }
            val resultSet: ResultSet = pstmt.executeQuery()
            while (resultSet.next()) {
                result = resultSet.getLong(1)
            }
            resultSet.close()
            pstmt.close()
        } catch {
            case e: Exception => e.printStackTrace()
        }
        result
    }

    def executeSql(connection: Connection, sql: String): Boolean = {
        var rtn: Boolean = false
        //todo Statement接口
        //Statement statement = connection.createStatement();
        //String sql1 = "insert into tb_student (name,age) values ('chy',20)";
        //statement.executeUpdate(sql1);
        //todo PreparedStatement接口
        var pstmt: PreparedStatement = null
        try {
            connection.setAutoCommit(false) // TODO: 关闭自动提交
            pstmt = connection.prepareStatement(sql)
            rtn = pstmt.execute()
            connection.commit()
            pstmt.close()
        }
        catch {
            case e: Exception => e.printStackTrace()
        }
        rtn
    }

    /**
     * 查询数据
     *
     * @param connection
     * @param sql
     * @return Json集合
     */
    def queryList(connection: Connection, sql: String): List[String] = {
        val resultList: ListBuffer[String] = new ListBuffer[String]()
        //创建连接对象
        val stat: Statement = connection.createStatement
        //执行语句
        val rs: ResultSet = stat.executeQuery(sql)
        //处理元数据 结果
        val md: ResultSetMetaData = rs.getMetaData
        import org.json4s._
        implicit val formats = DefaultFormats
        while (rs.next) {
            var rowData = Map[Any, Any]()
            //{"user_id":"123","name":"123"}
            for (i <- 1 to md.getColumnCount) {
                // TODO: 根据每列的类型名称进行匹配
                val value = md.getColumnTypeName(i).toLowerCase match {
                    case "int32" | "int" => rs.getInt(i)
                    case "uint64" | "long" => rs.getLong(i)
                    case _ => rs.getString(i)
                }
                //获取每个列的数据，并转换成json
                rowData += md.getColumnName(i) -> value

            }
            resultList += compact(Extraction.decompose(rowData))
        }
        stat.close()
        resultList.toList
    }

}
