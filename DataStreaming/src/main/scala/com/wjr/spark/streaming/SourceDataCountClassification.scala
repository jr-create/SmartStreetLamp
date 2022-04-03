package com.wjr.spark.streaming

import com.wjr.spark.bean.{DeviceCountInfo, DeviceInfo}
import com.wjr.spark.constant.RedisConstant
import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.sink.MyKafkaSink
import com.wjr.spark.utils._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, ForeachWriter, Row}
import org.json4s.jackson.JsonMethods
import redis.clients.jedis.Jedis

import java.lang.management.ManagementFactory
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.{lang, util}
import scala.collection.mutable.ListBuffer

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
            .option("kafka.bootstrap.servers", s"${MyKafkaSink.broker_list}")
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
            // .option("checkpointLocation", s"/checkpoint/dir/kafkaSource")
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
                        if (record != null) { // TODO:  数据不为空

                            import org.json4s._
                            implicit val formats = DefaultFormats // TODO: 需要放在函数内，来防止NotSerializableException
                            val json = record.getAs[String](0)
                            ProjectEnv.instance(json) // TODO: 缓存原始数据
                            val jValue = JsonMethods.parse(json)
                            val deviceInfo = jValue.extract[DeviceInfo]
                            // TODO: 将 地区Id 设备Id:设备类型:设备错误码 保存到redis中
                            val dt = sdf.format(new Date())
                            // TODO: Redis Key
                            val dauDeviceNormalKey = RedisConstant.dauDeviceNormalKey(deviceInfo.road_id.toString, dt) // TODO: 地区设备统计
                            val dauDeviceExceptKey = RedisConstant.dauDeviceExceptKey(deviceInfo.road_id.toString, dt) // TODO: 地区设备统计
                            val dauTypeKey = RedisConstant.dauTypeKey(deviceInfo.road_id.toString,dt) // TODO: 地区设备类型

                            if (deviceInfo.error_code == 1) { // TODO: 设备出现错误
                                // TODO: 添加异常的设备
                                jedisClient.sadd(dauDeviceExceptKey, deviceInfo.device_id)
                                //val errorDevices: util.Set[String] = jedisClient.smembers(dauDeviceExceptKey)
                                MyKafkaSink.send("dwd_error_device", json)
                            } else {
                                // TODO: 删除异常池中的设备Id
                                jedisClient.srem(dauDeviceExceptKey, deviceInfo.device_id)
                                // TODO: 添加正常的设备
                                val deviceIsExist = jedisClient.sadd(dauDeviceNormalKey, deviceInfo.device_id)
                                if (deviceIsExist == 1) {
                                    newDevice.append(deviceInfo)
                                }
                                MyKafkaSink.send("dwd_normal_device", json)
                            }
                            // TODO: 设备类型数
                            val typeIsExist: lang.Long = jedisClient.sadd(dauTypeKey, deviceInfo.type_id.toString)
                            if (typeIsExist == 1) {
                                newType.append(deviceInfo)
                            }
                            //设置key过期时间,如果没有过期时间，则设置
                            if (jedisClient.ttl(dauDeviceNormalKey) > 0 ){
                                jedisClient.expire(dauDeviceNormalKey, 3600 * 24)
                            }
                            if( jedisClient.ttl(dauDeviceExceptKey) > 0 ){
                                jedisClient.expire(dauDeviceExceptKey, 3600 * 24)
                            }
                            if (jedisClient.ttl(dauTypeKey) > 0) {
                                jedisClient.expire(dauTypeKey, 3600 * 24)
                            }
                            // TODO: 获取当天地区全部设备数量 并集-交集
                            val allDevice = jedisClient.sunion(dauDeviceNormalKey, dauDeviceExceptKey).size()-jedisClient.sinter(dauDeviceNormalKey,dauDeviceExceptKey).size()
                            val normalCount = jedisClient.scard(dauDeviceNormalKey)
                            val abnormalCount = jedisClient.scard(dauDeviceExceptKey)
                            val typeCount = jedisClient.scard(dauTypeKey)
                            val nowDate = dtf.format(new Date())
                            val deviceCountInfo = DeviceCountInfo(deviceInfo.road_id, newDevice.size, newType.size,
                                normalCount, abnormalCount, allDevice, typeCount, nowDate)
                            println("设备数量统计:" + deviceCountInfo)
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
