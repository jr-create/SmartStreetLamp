import com.wjr.spark.bean.DeviceInfo
import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.utils.{JsonUtils, SqlUtils}
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import java.lang.management.ManagementFactory

/**
 * @Package: com.wjr.spark.streaming
 * @ClassName: StructStreaming_Kafka
 * @author Lenovo-wjr
 * @create 2022-01-16 22:55
 * @Description:
 * 源数据转换
 */
object StructStreaming_DataSource extends App {
    val spark = SparkSession.builder
      .config("spark.sql.parquet.writeLegacyFormat", true)
      // TODO: 序列化保证
      .master("local[*]").appName("test")
      .enableHiveSupport().getOrCreate()
    val sparkContext = ProjectEnv.sparkContext

    import spark.implicits._

    // TODO: 进程号
    val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
    println(runtimeMXBean.getName().split("@")(0))
    // TODO: Schema
    val schema = new StructType()
        .add("key", "string")
        .add("value", "string")
        .add("topic", "string")
        .add("timestamp", "string")

    // TODO: Kafka Source
    val kafkaDF: DataFrame = spark.readStream.format("kafka")
        .option("kafka.bootstrap.servers", "hadoop01:9092")
        .option("subscribe", "wjr")
        .load()

    kafkaDF.printSchema()

    getKafkaJson
    spark.streams.awaitAnyTermination()

    private def getKafkaJson = {
        var fields = JsonUtils.ObjectToCastStr[DeviceInfo]
        fields ++= Array("cast(from_unixtime(t,'yyyy-MM-dd')as string) as dt")
        // TODO: 解析Kafka中json数据
        val kafkaJson = kafkaDF.selectExpr("cast(value as string) as v", "CAST(timestamp AS long) as t")
            .selectExpr(
                fields: _* // TODO: Array to string*
            )
        // TODO:创建ods表
        spark.sql(SqlUtils.createLampTable)

        // TODO: 写入到Sink端
        spark.sqlContext.setConf("hive.exec.dynamic.partition", "true")
        spark.sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")
        // TODO: foreachBatch
        kafkaJson.writeStream
            .option("checkpointLocation", s"checkpoint/dir/foreachBatch")
            .foreachBatch((ds: DataFrame, epochu_id: Long) => {
                ds.write.mode("append").format("hive")
                    .partitionBy("dt", "road_id")
                    .saveAsTable("lamp.ods_lamp_log")
                ()
            }).queryName("BatchToHive").start()

        val query2 = kafkaJson.writeStream
            .outputMode("append")
            .format("console")
            .queryName("console")
            .start()


        query2

    }

    private def WatermarkTest = {

        val kafkaDF2: DataFrame = spark.readStream.format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "wjr2")
            .load()
        val ds: Dataset[(String, String, String)] = kafkaDF.selectExpr("CAST(key AS string)",
            "CAST(value AS string) as v", "CAST(timestamp AS Timestamp) as t")
            .as[(String, String, String)]

        val query = ds.withWatermark("t", "1 second")
        //.groupBy(window($"t", "3 second", "2 second"), $"v")
        //.count()

        val watermarkDS = kafkaDF2.selectExpr("CAST(key AS string)", "CAST(value AS string) as wv", "CAST(timestamp AS Timestamp) as wt")
            .withWatermark("wt", delayThreshold = "5 second")
        //.groupBy(window($"wt", "5 second", "2 second"), $"wv")
        //.count()
        // TODO: 双流join
        query.join(watermarkDS,
            expr(
                """
                  |v = wv AND
                  |wt >= t and
                  |wt <= t + interval 5 second
                  |""".stripMargin)
            , joinType = "leftOuter" // can be "inner", "leftOuter", "rightOuter", "fullOuter", "leftSemi"
        ).writeStream.format("console").outputMode("append").queryName("join").option("truncate", "false").start()
        watermarkDS.dropDuplicates("key", "wv", "wt") // TODO: 删除流式重复数据
        watermarkDS.writeStream
            .format("console")
            .outputMode("update")
            .option("truncate", "false") // TODO: 显示完整列内容
            .start()
    }
}
