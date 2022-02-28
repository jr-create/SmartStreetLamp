package com.wjr.spark.streaming

import com.wjr.spark.bean.{DeviceCountInfo, DeviceInfo, LampCurrencyIndex}
import com.wjr.spark.constant.RedisConstant
import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.sink.{ClickhouseSink, MyKafkaSink}
import com.wjr.spark.utils.{ClickhouseJdbcUtil, FieldAndValue, JsonUtils, LazyLogging, MyRedisUtil, PhoenixJdbcUtil, SqlUtils}
import org.apache.spark.sql.{DataFrame, Dataset, ForeachWriter, Row, SaveMode}
import org.apache.spark.sql.streaming.Trigger
import org.json4s.jackson.{JsonMethods, Serialization}
import redis.clients.jedis.Jedis

import java.{lang, util}
import java.lang.management.ManagementFactory
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
 * 原始数据分散
 * 设备数量统计
 */
object SourceDataCountClassification extends LazyLogging {
    val spark = ProjectEnv.spark
    val sparkContext = ProjectEnv.sparkContext

    import spark.implicits._

    def main(args: Array[String]): Unit = {

        /**
         * 打印进程号
         */
        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        println(runtimeMXBean.getName().split("@")(0))

        // TODO:1、获取Kafka数据 Kafka Source
        val kafkaDF: DataFrame = spark.readStream
            .format("kafka")
            .option("kafka.bootstrap.servers", "hadoop01:9092")
            .option("subscribe", "ods_lamp_log,dwd_device_count")
            .load()

        // TODO: 2、选择Kafka的列属性
        val wjrDF = kafkaDF.selectExpr("topic", "cast(value as string) as v", "CAST(timestamp AS timestamp) as t")
            .na.drop("any") // TODO: 去掉任意字段为null的行
            .where('topic === "ods_lamp_log") // TODO: 过滤topic不为wjr的行

        // TODO: 3、处理json数据
        outputStream(wjrDF)
        //testConsole(wjrDF,"wjrDF")
        /**
         * .na.drop("any"):表示当某行中特定列任意一个字段为 null 或者 NaN 的时候丢弃此行
         */
        val wjr2Json = kafkaDF.selectExpr("topic", "cast(value as string) as v")
            .na.drop("any") // TODO: 去掉任意字段为null的行
            .where('topic =!= "ods_lamp_log") // TODO: 过滤topic不为wjr的行
            .select("v")
        testConsole(wjr2Json, "wjr2Json")

        spark.streams.awaitAnyTermination()
        //spark.close()
    }


    def testConsole(dataFrame: DataFrame, queryName: String): Unit = {
        dataFrame.writeStream
            .format("console")
            .outputMode("append")
            .queryName(queryName)
            .start()
    }

    def outputStream(dataFrame: DataFrame): Unit = {
        var fields = JsonUtils.ObjectToCastStr[DeviceInfo]
        // TODO: 解析Kafka中json数据
        val kafkaJson: DataFrame = dataFrame.selectExpr("v", "t")
        // TODO: 4、写入hive, 对应hive表的分区（dt,road_id）；lines
        dataFrame.withWatermark("t", delayThreshold = "10 second").selectExpr(
            "cast(from_unixtime(cast(t as long),'yyyy-MM-dd')as string) as dt",
            "cast(get_json_object(v,'$.road_id') as string) as road_id",
            "cast(v as string) as lines").writeStream
            .option("checkpointLocation", s"hdfs://hadoop01:8020/checkpoint/dir/foreachBatch")
            .foreachBatch((ds: DataFrame, epochu_id: Long) => {
                ds.write.mode("append").format("hive")
                    .partitionBy("dt", "road_id")
                    .saveAsTable("lamp.ods_lamp_log")
                ()
            }).queryName("SourceToHive").start()

        // TODO: 5、解析json后向Kafka写入
        kafkaJson.withWatermark("t", delayThreshold = "10 second")
            .writeStream
            .option("checkpointLocation", s"hdfs://hadoop01:8020/checkpoint/dir/kafkaSource")
            .foreach(
                new ForeachWriter[Row] {
                    val newType: ListBuffer[DeviceInfo] = ListBuffer.empty[DeviceInfo] // TODO: 新增设备数
                    val newDevice: ListBuffer[DeviceInfo] = ListBuffer.empty[DeviceInfo] // TODO: 新增类型数
                    var jedisClient: Jedis = _ // TODO: redis客户端
                    var phoenixConnection: Connection = _ // TODO: phoenix JDBC客户端
                    var clickHouseConnection: Connection = _ // TODO: Ck JDBC客户端
                    val sdf = new SimpleDateFormat("yyyy-MM-dd") // TODO: 时间格式
                    val dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss") // TODO: 时间格式

                    def open(partitionId: Long, version: Long): Boolean = {
                        println("streaming client open ")
                        jedisClient = MyRedisUtil.getJedisClient
                        //phoenixConnection = PhoenixJdbcUtil.getConnection
                        clickHouseConnection = ClickhouseJdbcUtil.getConnection
                        true
                    }

                    def process(record: Row): Unit = {
                        println("元数据：" + record)
                        if (record != null) { // TODO:  数据不为空

                            import org.json4s._
                            implicit val formats = DefaultFormats // TODO: 需要放在函数内，来防止NotSerializableException
                            //implicit val formats = Serialization.formats(NoTypeHints)
                            //for (i <- 0 until record.size) {
                            val json = record.getAs[String](0)
                            ProjectEnv.instance(json) // TODO: 缓存原始数据
                            val jValue = JsonMethods.parse(json)
                            val deviceInfo = jValue.extract[DeviceInfo]
                            // TODO: 将 地区Id 设备Id:设备类型:设备错误码 保存到redis中
                            val dt = sdf.format(new Date())
                            // TODO: Redis Key
                            val dauDeviceNormalKey = RedisConstant.dauDeviceNormalKey(deviceInfo.road_id.toString, dt) // TODO: 地区设备统计
                            val dauDeviceExceptKey = RedisConstant.dauDeviceExceptKey(deviceInfo.road_id.toString, dt) // TODO: 地区设备统计
                            val dauTypeKey = RedisConstant.dauTypeKey(deviceInfo.road_id.toString, dt) // TODO: 地区设备类型

                            if (deviceInfo.error_code == 1) { // TODO: 设备出现错误
                                // TODO: 添加异常的设备
                                jedisClient.sadd(dauDeviceExceptKey, deviceInfo.device_id)
                                //val errorDevices: util.Set[String] = jedisClient.smembers(dauDeviceExceptKey)
                                //MyKafkaSink.send("dwd_device_error", json)
                                val jsonData = JsonUtils.jsonToList(json).filter(!_.toString.contains("test")).map(x => {
                                    val value = x.toString.split(" ")
                                    (value(0).split("\\.").last, value(1), value(2))
                                })
                                logger.info(s"[${this.getClass.getSimpleName}] 错误设备json解析：$jsonData")
                                // TODO: 将错误设备信息保存到CK中，建表，插入
                                ClickhouseJdbcUtil.executeSql(clickHouseConnection,
                                    SqlUtils.createCKTable("dwd_error_device", jsonData.map(x => (x._1, x._2)).toMap, "MergeTree", "road_id", "toYYYYMMDD(toDate(timestamp))", "device_id")
                                        + " TTL toDate(timestamp) + INTERVAL 1 MINUTE")
                                ClickhouseJdbcUtil.executeSql(clickHouseConnection, SqlUtils.insertSql("dwd_error_device", jsonData.map(_._3).toBuffer))
                            } else {
                                // TODO: 删除异常池中的设备Id
                                jedisClient.srem(dauDeviceExceptKey, deviceInfo.device_id)
                                // TODO: 添加正常的设备
                                val deviceIsExist = jedisClient.sadd(dauDeviceNormalKey, deviceInfo.device_id)
                                if (deviceIsExist == 1) {
                                    newDevice.append(deviceInfo)
                                }
                                MyKafkaSink.send("dwd_device_normal", json)
                            }
                            // TODO: 设备类型数
                            val typeIsExist: lang.Long = jedisClient.sadd(dauTypeKey, deviceInfo.type_id.toString)
                            if (typeIsExist == 1) {
                                newType.append(deviceInfo)
                            }
                            //设置key过期时间
                            if (jedisClient.ttl(dauDeviceNormalKey) > 0 && jedisClient.ttl(dauDeviceExceptKey) > 0 && jedisClient.ttl(dauTypeKey) > 0) { //如果没有过期时间，则设置
                                jedisClient.expire(dauDeviceNormalKey, 3600 * 24)
                                jedisClient.expire(dauDeviceExceptKey, 3600 * 24)
                                jedisClient.expire(dauTypeKey, 3600 * 24)
                            }
                            println("环境传感器指标：" + deviceInfo.values)
                            // TODO: 通过差集获取总的设备数（排除两个队列中都有的设备）
                            val allDevice: util.Set[String] = jedisClient.sdiff(dauDeviceNormalKey, dauDeviceExceptKey)
                            val normalCount = jedisClient.scard(dauDeviceNormalKey)
                            val abnormalCount = jedisClient.scard(dauDeviceExceptKey)
                            val typeCount = jedisClient.scard(dauTypeKey)
                            val nowDate = dtf.format(new Date())
                            val deviceCountInfo = DeviceCountInfo(deviceInfo.road_id, newDevice.size, newType.size,
                                normalCount, abnormalCount, allDevice.size(), typeCount, nowDate)
                            try {
                                // TODO: 设备数量统计表保存到Hbase中
                                //PhoenixJdbcUtil.executeSql(phoenixConnection,SqlUtils.insertPhoenixData("dwd_device_count",
                                //    JsonUtils.getObjectValue(deviceCountInfo)))
                                // TODO: 设备数量统计表保存到CK中
                                logger.info("设备数量统计:" + deviceCountInfo)
                                ClickhouseJdbcUtil.executeSql(clickHouseConnection, SqlUtils.insertSql("dwd_device_count",
                                    JsonUtils.getObjectValue(deviceCountInfo).toBuffer))

                                MyKafkaSink.send("dwd_device_count", JsonUtils.ObjectToJson(deviceCountInfo))
                            } catch {
                                case e => e.printStackTrace()
                            }
                            //}
                        }
                    }

                    def close(errorOrNull: Throwable): Unit = {

                        jedisClient.close()
                        //phoenixConnection.close()
                        clickHouseConnection.close()
                        println("client stop")
                    }
                }
            ).trigger(Trigger.ProcessingTime(5, TimeUnit.SECONDS))
            .queryName("SourceToKafka")
            .start()

    }

}
