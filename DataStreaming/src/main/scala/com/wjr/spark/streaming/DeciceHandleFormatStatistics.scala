package com.wjr.spark.streaming

import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.sink.{ClickhouseSink, MyKafkaSink}
import com.wjr.spark.utils.{ClickhouseJdbcUtil, JsonUtils, LazyLogging, SqlUtils}
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.lang.management.ManagementFactory

/**
 * @Package: com.wjr.spark.streaming
 * @ClassName: DeviceValueHandle
 * @author Lenovo-wjr
 * @create 2022-02-17 1:20
 * @Description: 正常数据结构化，异常数据发送后端展示
 */
object DeciceHandleFormatStatistics extends LazyLogging {


 def DeciceHandleFormat(spark: SparkSession): Unit = {
   import spark.implicits._

   val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
    logger.info(runtimeMXBean.getName().split("@")(0))

    // 1、获取Kafka数据 Kafka Source =》Json数据
    val kafkaDF: DataFrame = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", s"${MyKafkaSink.broker_list}")
      .option("subscribe", "dwd_normal_device,dwd_error_device")
      .option("failOnDataLoss", "false")
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("topic", "cast(value as string) as v", "CAST(timestamp AS timestamp) as t")
      .na.drop("any") // 去掉任意字段为null的行

    // json 解析成 List((名称 类型 值),(名称 类型 值))
    val jsonList = JsonUtils.jsonToList(ProjectEnv.json)
    // json 转为（创建hive表语句，插入hive语句，sparkSQL解析语句）
    val tuple = JsonUtils.jsonListToHSql("dwd", "lamp", "dwd_normal_device", jsonList)
    spark.sql(tuple._1) // 创建 dwd_normal_device hive表
    // spark.sql(tuple._2).show // 插入缓存的第一条设备数据

    // Clickhouse 连接
    val clickHouseConnection = ClickhouseJdbcUtil.getConnection()

    // 解析json数据
    val jsonDate = jsonList.filter(!_.toString.contains("test")).map(x => {
      val value = x.toString.split(" ")
      (value(0).split("\\.").last, value(1), value(2))
    })

    // Clickhouse 创建 dwd_normal_device 正常设备信息表
    ClickhouseJdbcUtil.executeSql(clickHouseConnection,
      SqlUtils.createCKTable("dwd_normal_device", jsonDate.map(x => (x._1, x._2)) ++ List(("dt", "String")),
        "MergeTree", "device_id,road_id", "toYYYYMMDD(toDateTime(timestamp))",
        "device_id"))
    // Clickhouse 创建 dwd_error_device 异常设备信息表
    ClickhouseJdbcUtil.executeSql(clickHouseConnection,
      SqlUtils.createCKTable("dwd_error_device", jsonDate.map(x => (x._1, x._2)) ++ List(("dt", "String")),
        "MergeTree", "device_id,road_id", "toYYYYMMDD(toDateTime(timestamp))",
        "device_id")
        + "TTL toDateTime(timestamp) + INTERVAL 5 MINUTE")
    // ClickhouseJdbcUtil.executeSql(clickHouseConnection, SqlUtils.insertSql("dwd_error_device", jsonDate.map(_._3).toBuffer))

    val normalDeviceDF = kafkaDF.where('topic === "dwd_normal_device") // TODO: 选择topic
    val errorDeviceDF = kafkaDF.where('topic === "dwd_error_device") // TODO: 选择topic

    // Kafka 正常设备信息保存到hive和Ck中
    handleNormalStream(normalDeviceDF, "dwd_normal_device", tuple)
    // Kafka 异常设备信息保存到CK中
    handleErrorStream(errorDeviceDF, "dwd_error_device", tuple)

  }

  /**
   * 3、将从kafka读取的json数据结构化到hive中
   *
   * @param dataFrame
   */
  def handleNormalStream(dataFrame: DataFrame, tbName: String, tuple: (String, String, List[String])): Unit = {

    dataFrame.withWatermark("t", delayThreshold = "10 second").selectExpr(
      tuple._3: _*
    ).writeStream
      .option("checkpointLocation", s"hdfs://hadoop01:8020/checkpoint/dir/NormalJsonToDwdHive")
      // .option("checkpointLocation", s"checkpoint/dir/JsonToDwdHive")
      .foreachBatch((ds: DataFrame, epochu_id: Long) => {
        // 写入Hive
        ds.write.mode("append").format("hive")
          .partitionBy("dt", "road_id")
          .saveAsTable(s"lamp.$tbName")
        // ds.drop("test").join(ckDF, "road_id").show
        // 写入Clickhouse
        ds.drop("test")
          .write.mode("append")
          .jdbc(ClickhouseSink.propertiesMap.get("url").get, tbName, ClickhouseSink.prop)
        ()
      })
      .queryName("Normal_Dwd_Hive_CK").start()
  }

  /**
   * 4、将异常的设备信息发送给后端展示
   *
   * @param dataFrame
   * @param topicName
   * @param tuple
   */
  def handleErrorStream(dataFrame: DataFrame, tbName: String, tuple: (String, String, List[String])): Unit = {

    dataFrame.withWatermark("t", delayThreshold = "10 second").selectExpr(
      tuple._3: _*
    ).writeStream
      .option("checkpointLocation", s"hdfs://hadoop01:8020/checkpoint/dir/ErrorJsonToDwdCK")
      // .option("checkpointLocation", s"checkpoint/dir/JsonToDwdCK")
      .foreachBatch((ds: DataFrame, epochu_id: Long) => {
        // TODO: 写入Clickhouse
        ds.drop("test")
          .write.mode("append")
          .jdbc(ClickhouseSink.propertiesMap.get("url").get, tbName, ClickhouseSink.prop)
        ()
      })
      .queryName("Error_Dwd_CK").start()
    //.join(ckDF, "road_id")
    //.select(to_json(struct("*")))
    //.toDF("value")
    //// TODO: 测试
    ////.writeStream.format("console").outputMode("append").start()
    //// TODO: 写入Kafla
    //.writeStream
    //.format("kafka")
    //.option("checkpointLocation", s"checkpoint/dir/JsonJoinCK")
    //.option("kafka.bootstrap.servers", "hadoop01:9092")
    //.option("topic", s"$topicName")
    //.queryName("JsonJoinCK")
    //.start()
  }
}
