import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.sink.{ClickhouseSink, MyKafkaSink}
import com.wjr.spark.streaming.DeciceHandleFormatStatistics.spark
import com.wjr.spark.utils.JsonUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * @Package: com.wjr.spark.streaming
 * @ClassName: DeviceDataStatistics
 * @author Lenovo-wjr
 * @create 2022-02-20 15:36
 * @Description: 正常设备数据统计
 */
object DeviceDataStatistics {
  val spark = SparkSession.builder
    .config("spark.sql.parquet.writeLegacyFormat", true)
    // TODO: 序列化保证
    .master("local[*]").appName("testS")
    .enableHiveSupport().getOrCreate()
  val sparkContext = ProjectEnv.sparkContext

  /**
   * 统计指标
   */
  val json =
    """
      |{"reason":"success","device_id":"my_00721","type_id":6,"timestamp":1645114771,"error_code":0,"road_id":6,"longitude":"116.38200833778303","latitude":"39.92425352469552","values":{"voltage":"0","temperature":"0.0","humidity":"0","lighting":"0","PM2_5":"0","CO_2":"0","info":"yin阴","direct":"xibeifeng西北风","power":"3级"},"test":["a1",2]}
      |""".stripMargin

  //val json = ProjectEnv.instance()
  def main(args: Array[String]): Unit = {
    //val ckDF = spark.read.format("jdbc")
    //    .options(ClickhouseSink.propertiesMap.toMap)
    //    .option("dbtable", "mysql8.dwd_road_management_view")
    //    .load()

    //val deviceIndex = spark.sql(
    //    s"""
    //       |select * from lamp.dwd_device_index
    //       |""".stripMargin).drop("test")
    //    .join(ckDF, "road_id")
    //    .write.mode("append")
    //    .jdbc(ClickhouseSink.propertiesMap.get("url").get, "dws_device_managment", ClickhouseSink.prop)
    //
    //spark.sql(
    //    s"""
    //       |select road_id,${avgIndexes.mkString(",")}
    //       |from lamp.dwd_device_index
    //       |group by road_id
    //       |""".stripMargin).join(ckDF, "road_id").show()


    // json 解析成 List((名称 类型 值),(名称 类型 值))
    val jsonList = JsonUtils.jsonToList(ProjectEnv.json)
    // json 转为（创建hive表语句，插入hive语句，sparkSQL解析语句）
    val tuple = JsonUtils.jsonListToHSql("dwd", "lamp", "dwd_normal_device_test", jsonList)
    spark.sql(tuple._1) // 创建 dwd_normal_device hive表
    spark.sql(tuple._2).show // 插入缓存的一天数据
    spark.sql("select * from lamp.dwd_normal_device_test").printSchema(1)

  }

}
