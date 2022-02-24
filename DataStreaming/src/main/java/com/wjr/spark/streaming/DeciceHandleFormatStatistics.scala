package com.wjr.spark.streaming

import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.sink.ClickhouseSink
import com.wjr.spark.streaming.SourceDataCountClassification.{outputStream, spark, testConsole}
import com.wjr.spark.utils.JsonUtils
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{expr, struct, to_json}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.lang.management.ManagementFactory
import java.util.Properties

/**
 * @Package: com.wjr.spark.streaming
 * @ClassName: DeviceValueHandle
 * @author Lenovo-wjr
 * @create 2022-02-17 1:20
 * @Description: 正常数据结构化，异常数据发送后端展示
 */
object DeciceHandleFormatStatistics {
    val spark = ProjectEnv.spark
    val sparkContext = ProjectEnv.sparkContext

    import spark.implicits._


    def main(args: Array[String]): Unit = {
        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        println(runtimeMXBean.getName().split("@")(0))

        // TODO:1、获取Kafka数据 Kafka Source =》Json数据
        val kafkaDF: DataFrame = spark.readStream
            .format("kafka")
            .option("kafka.bootstrap.servers", "hadoop01:9092")
            .option("subscribe", "dwd_device_normal,dwd_device_error")
            .option("failOnDataLoss", "false")
            .load()
            .selectExpr("topic", "cast(value as string) as v", "CAST(timestamp AS timestamp) as t")
            .na.drop("any") // TODO: 去掉任意字段为null的行
        // TODO: 2、获取clickhouse中的地区人员管理信息
        val ckDF = spark.read.format("jdbc")
            .options(ClickhouseSink.propertiesMap.toMap)
            .option("dbtable", "mysql8.dwd_road_management_view")
            .load()

        ckDF.show()
        val tuple = JsonUtils.jsonListToHSql("dwd", "lamp", "dws_device_management", JsonUtils.jsonToList(ProjectEnv.json))
        //spark.sql(tuple._1) // TODO: 创建表
        //spark.sql(tuple._2).show

        // TODO: 2、选择Kafka的schema（列属性）
        val errorDeviceDF = kafkaDF.where('topic === "dwd_device_error") // TODO: 选择topic

        val normalDeviceDF = kafkaDF.where('topic === "dwd_device_normal") // TODO: 选择topic

        // TODO: 3、将从kafka读取的json数据结构化到hive中
        //正常设备信息保存到hive表中
        handleNormalStream(normalDeviceDF, ckDF, "dws_device_management", tuple)
        // TODO: 4、将异常的设备信息发送给后端展示
        //错误的信息发送给后端，以便交互使用
        //handleErrorStream(errorDeviceDF,ckDF,  "dws_device_error_handle", tuple)
        // TODO: 5、开启等待
        spark.streams.awaitAnyTermination()
    }

    /**
     * 3、将从kafka读取的json数据结构化到hive中
     *
     * @param dataFrame
     */
    def handleNormalStream(dataFrame: DataFrame, ckDF: DataFrame, tbName: String, tuple: (String, String, List[String])): Unit = {

        dataFrame.withWatermark("t", delayThreshold = "10 second").selectExpr(
            tuple._3: _*
        ).writeStream
            //.option("checkpointLocation", s"hdfs://hadoop01:8020/checkpoint/dir/JsonToDwdHive")
            .option("checkpointLocation", s"checkpoint/dir/JsonToDwdHive")
            .foreachBatch((ds: DataFrame, epochu_id: Long) => {

                //ds.write.mode("append").format("hive")
                //    .partitionBy("dt", "road_id")
                //    .saveAsTable(s"lamp.$tbName")
                ds.drop("test").join(ckDF, "road_id").show
                // TODO: 写入Clickhouse
                ds.drop("test").join(ckDF, "road_id")
                    .write.mode("append")
                    .jdbc(ClickhouseSink.propertiesMap.get("url").get, tbName, ClickhouseSink.prop)
                ()
            })
            .queryName("JsonToDwdHive").start()
    }

    /**
     * 4、将异常的设备信息发送给后端展示
     *
     * @param dataFrame
     * @param topicName
     * @param tuple
     */
    def handleErrorStream(dataFrame: DataFrame, ckDF: DataFrame, topicName: String, tuple: (String, String, List[String])): Unit = {

        dataFrame.withWatermark("t", delayThreshold = "10 second").selectExpr(
            tuple._3: _*
        )
            .join(ckDF, "road_id")
            .select(to_json(struct("*")))
            .toDF("value")
            // TODO: 测试
            //.writeStream.format("console").outputMode("append").start()
            .writeStream
            .format("kafka")
            .option("checkpointLocation", s"checkpoint/dir/JsonJoinCK")
            .option("kafka.bootstrap.servers", "hadoop01:9092")
            .option("topic", s"$topicName")
            .queryName("JsonJoinCK")
            .start()
    }
}
