import com.wjr.spark.sink.{ClickhouseSink, MyKafkaSink}
import org.apache.spark.SparkConf
import org.apache.spark.sql.functions.{struct, to_json}
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.lang.management.ManagementFactory

/**
 * @Package:
 * @ClassName: StreamHiveToCk
 * @author Lenovo-wjr
 * @create 2022-02-20 18:05
 * @Description:
 */
object StreamHiveToCk {
    def main(args: Array[String]): Unit = {

        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        println(runtimeMXBean.getName().split("@")(0))
        val conf = new SparkConf().setMaster("local[2]").setAppName("hive")
        val spark = SparkSession.builder().enableHiveSupport().config(conf).getOrCreate()
        val kafkaDF: DataFrame = spark.readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", s"${MyKafkaSink.broker_list}")
          .option("subscribe", "test")
          .option("failOnDataLoss", "false")
          .option("startingOffsets", "earliest")
          .load()
          .selectExpr("topic", "cast(value as string) as road_id", "CAST(timestamp AS timestamp) as t")
          .na.drop("any") // 去掉任意字段为null的行
        val ckDF = spark.read.format("jdbc")
          .options(ClickhouseSink.propertiesMap.toMap)
          .option("dbtable", "mysql8.dwd_road_management_view")
          .load()
        ckDF.join(kafkaDF,"road_id")
          .select(to_json(struct("*")))
          .toDF("value")
          .writeStream.format("console")
          .option("truncate", "false")// 显示完整列内容
          .outputMode("append")
          .start()
        //// 5、开启等待
        spark.streams.awaitAnyTermination()
    }

}
