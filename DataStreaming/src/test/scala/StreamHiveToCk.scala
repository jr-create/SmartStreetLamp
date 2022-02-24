import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.sink.ClickhouseSink
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.hive.HiveContext

import java.lang.management.ManagementFactory
import java.util
import java.util.Properties

/**
 * @Package:
 * @ClassName: StreamHiveToCk
 * @author Lenovo-wjr
 * @create 2022-02-20 18:05
 * @Description:
 */
object StreamHiveToCk {
    def main(args: Array[String]): Unit = {
        val conf = new SparkConf().setMaster("local[2]").setAppName("hive")
        val spark = SparkSession.builder().enableHiveSupport().config(conf).getOrCreate()
        val sparkContext = ProjectEnv.sparkContext
        val hc = new HiveContext(sparkContext)

        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        println(runtimeMXBean.getName().split("@")(0))

        // TODO: Kafka Source
        //val kafkaDF: DataFrame = spark.readStream
        //    .format("kafka")
        //    .option("kafka.bootstrap.servers", "localhost:9092")
        //    .option("subscribe", "wjr,wjr2")
        //    .load()
        //val wjrDF = kafkaDF.selectExpr("topic", "cast(value as string) as v")
        //    .na.drop("any") // TODO: 去掉任意字段为null的行
        //    .where('topic === "wjr") // TODO: 过滤topic不为wjr的行

        //spark.sql(
        //    """
        //      |insert into test values("lili","2022-02-15 11:00:00")
        //      |""".stripMargin).show()

        val properties = new Properties
        import scala.collection.JavaConverters.mapAsJavaMap
        val map: util.Map[String, String] = mapAsJavaMap(ClickhouseSink.propertiesMap.toMap)
        properties.putAll(map)
        val url = "jdbc:clickhouse://localhost:8123/default"
        val table = "test"

        /**
         * 将hive的数据写入ck中
         */
        spark.sql(
            """
              |select * from default.test
              |""".stripMargin)
            .write.mode("append").jdbc(url, table, properties)
        /**
         * 读取kafka数据
         */
        val ckFrame = spark.read.format("jdbc")
            .options(ClickhouseSink.propertiesMap.toMap)
            .option("dbtable", "dwd_road_management_view")
            .load()
        ckFrame.show()
        /**
         * kafka数据和ck中数据join
         */
        val kafkaDF = spark.readStream.format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "wjr,wjr2")
            .load().selectExpr("topic", "cast(value as string) as v")
            .na.drop("any") // TODO: 去掉任意字段为null的行
            .selectExpr("cast(get_json_object(v,'$.name') as string ) as kname")
            .join(ckFrame, expr(
                """sh
                  |kname = name
                  |""".stripMargin)
                , joinType = "leftOuter")
            .writeStream.format("console").outputMode("append")
            .start()

        spark.streams.awaitAnyTermination()

    }

}
