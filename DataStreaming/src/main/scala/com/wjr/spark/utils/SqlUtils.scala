package com.wjr.spark.utils

import com.wjr.spark.bean.{DeviceCountInfo, Sensor}
import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.streaming.DeciceHandleFormatStatistics.spark
import com.wjr.spark.utils.JsonUtils.getObjectValue
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @Package: com.wjr.spark.utils
 * @ClassName: SqlUtils
 * @author Lenovo-wjr
 * @create 2022-01-31 14:52
 * @Description:
 */
object SqlUtils {
    final val createLampTable =
        """
          |CREATE EXTERNAL TABLE if not exists lamp.ods_lamp_log (
          |`reason`  string,
          |`device_id` string,
          |`type_id` string,
          |`error_code` string,
          |`longitude` string,
          |`latitude` string,
          |`values` string,
          |`timestamp` string
          |)
          |PARTITIONED BY (`dt` string,road_id string)
          |STORED AS parquet
          |LOCATION '/warehouse/smartStreetLamp/ods/ods_lamp_log'
          |tblproperties("parquet.compression"="snappy")
          |""".stripMargin

    /**
     * 创建hive的外部表
     *
     * @param dbName
     * @param tbName
     * @param tbType
     * @param option
     * @return
     */
    final def createExternalTable(dbName: String, tbName: String, tbType: String, option: Map[String, String]) =
        s"""
           |CREATE EXTERNAL TABLE $dbName.$tbName(
           |${option.map(x => s"${x._1} ${x._2}").mkString(",")}
           |)PARTITIONED BY (`dt` string,road_id string)
           |STORED As parquet
           |LOCATION '/warehouse/smartStreetLamp/${tbType}/${tbName}'
           |""".stripMargin

    final val deviceCountTable =
        """
          |CREATE TABLE EXTERNAL TABLE if not exists  lamp.dwd_device_count (
          |  `device_id` string,
          |  `device_type` INT,
          |  `road_id` INT,
          |  `timestamp` INT,
          |  `normal_count` INT,
          |  `abnormal_count` INT,
          |  `device_total` INT,
          |  `type_name` string,
          |  `create_time` string,
          |  `road_name` string,
          |  `management` string,
          |  `city_name` string,
          |  `province_name` string,
          |  `province_area_code` string,
          |  `province_iso_code` string
          |)

          |""".stripMargin

    /**
     * 插入hive多条数据
     * insert into ${tbName} VALUES (x,x,x,x,),(x,x,x,x,x)
     * @param tbName
     * @param values
     * @return
     */
    final def insertBulk(tbName: String, values: ArrayBuffer[ArrayBuffer[String]]): String = {
        val valueStr = values.map(x => {
            "(" + x.mkString(",") + ")"
        })
        s"""
           |insert into ${tbName} VALUES
           |${valueStr.mkString(",")}
           |""".stripMargin
    }

    final def insertSql(tbName: String, option: mutable.Buffer[AnyRef]): String = {
        s"""
           |insert into ${tbName}
           |Values(${
            option.map(x => {
                x.getClass.getTypeName.split("\\.").toBuffer.last match {
                    case "String" => s"'$x'"
                    case _ => x
                }
            }).mkString(",")
        });
           |""".stripMargin
    }

    def insertPhoenixData(tbName: String, option: Array[AnyRef]): String = {
        s"""
           |upsert into $tbName Values(${
            option.map(x => {
                x.getClass.getTypeName.split("\\.").toBuffer.last match {
                    case "String" => s"'$x'"
                    case _ => x
                }

            }).mkString(",")
        })
           |""".stripMargin
    }

    /**
     * 创建clickhouse表
     *
     * @return
     */
    def createCKTable(tbName: String, option: List[Tuple2[String, String]], engine: String,
                      orderBy: String,
                      partitionBy: String,
                      primaryKey: String = null
                     ): String = {
        s"""
           |create table if not exists $tbName (
           |${option.map(x => s"${x._1} ${x._2}").mkString(",")}
           |)engine =$engine
           |partition by ($partitionBy)
           |${if (primaryKey != null) s"primary key ($primaryKey)"}
           |order by ($orderBy)
           |""".stripMargin
    }

    def createHbasePhoenixTable(tbName: String, option: Map[String, String]): String = {
        s"""
           |create table ${tbName.toUpperCase} (
           |${option.map(x => s"${x._1} ${x._2}").mkString(",")}
           |)
           |""".stripMargin
    }


    private val DeviceCountInfoOption = Map("road_id" -> "String",
        "road_name" -> "String",
        "new_device_count" -> "UInt64",
        "new_type_count" -> "UInt64",
        "normal_count" -> "UInt64",
        "abnormal_count" -> "UInt64",
        "device_total" -> "UInt64",
        "type_count" -> "UInt64",
        "create_time" -> "DateTime")

    private val DwsRoadManagerOption = DeviceCountInfoOption ++ Map(
        "management" -> "String",
        "city_name" -> "String",
        "province_name" -> "String",
        "province_area_code" -> "String",
        "province_iso_code" -> "String")

    val regionInfo = Map(
        "road_id" -> "String",
        "road_name" -> "String",
        "city_id" -> "String"
    )

    /**
     * 设备传感器状态表
     */
    val sensor_status1 = Map(
        "device_id" -> "String",
        "road_id" -> "Int32",
        "create_time" -> "DateTime"
    ) ++ JsonUtils.getFields[Sensor].map(x => (s"${x}_status", "Int32")).toMap

    val sensor_status2 = Map(
        "device_id" -> "varchar",
        "road_id" -> "bigint",
        "create_time" -> "varchar"
    ) ++ JsonUtils.getFields[Sensor].map(x => (s"${x}_status", "bigint")).toMap

    /**
     * 地区路灯Value
     */
    val device_value = Map(
        "" -> ""
    )

    def mergeSmallFile(dbName: String, tbName: String): String = {
        s"""
           |insert overwrite table $dbName.$tbName
           | partition (dt,road_id)
           |select * from $dbName.$tbName
           |""".stripMargin
    }

    def main(args: Array[String]): Unit = {
        val spark = ProjectEnv.spark
        //
        //// TODO: 小文件合并 每天执行一次，前提开启动态分区（在ProjectEnv中 ）
        spark.sql(mergeSmallFile("lamp", "dwd_normal_device"))
        // TODO: hive-ods表
        //val odsLampLogTable = createExternalTable("lamp", "ods_lamp_log", "ods", Map("lines" -> "string"))

        //// TODO: clickhouse的地区数量表
        //val dwd_device_count = createCKTable("dwd_device_count", DeviceCountInfoOption,
        //    "ReplacingMergeTree(create_time)", "road_id", "road_id", "road_id")
        //// TODO: hbase的地区数量表
        //val DeviceCountInfo = createHbasePhoenixTable("DeviceCountInfo", DeviceCountInfoOption)
        //
        //// TODO:Clickhouse的传感器状态表
        //val dwd_sensor_status1 = createCKTable("dwd_sensor_status", sensor_status1,
        //    "ReplacingMergeTree(create_time)", "device_id", "device_id", "device_id")
        //
        //// TODO:  hbase的传感器状态表
        //val dwd_sensor_status2 = createHbasePhoenixTable("dwd_sensor_status", sensor_status2)
        //println(dwd_sensor_status2)

        spark.close()

    }
}
