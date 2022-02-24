import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.sink.ClickhouseSink
import com.wjr.spark.utils.JsonUtils

/**
 * @Package: com.wjr.spark.streaming
 * @ClassName: DeviceDataStatistics
 * @author Lenovo-wjr
 * @create 2022-02-20 15:36
 * @Description: 正常设备数据统计
 */
object DeviceDataStatistics {
    //val spark = ProjectEnv.spark
    //val sparkContext = ProjectEnv.sparkContext

    /**
     * 统计指标
     */
    val json =
        """
          |{"reason":"success","device_id":"my_00721","type_id":6,"timestamp":1645114771,"error_code":0,"road_id":6,"longitude":"116.38200833778303","latitude":"39.92425352469552","values":{"voltage":"0","temperature":"0.0","humidity":"0","lighting":"0","PM2_5":"0","CO_2":"0","info":"yin阴","direct":"xibeifeng西北风","power":"3级"},"test":["a1",2]}
          |""".stripMargin

    //val json = ProjectEnv.instance()
    def main(args: Array[String]): Unit = {
        val jsonList = JsonUtils.jsonToList(json)
            .filter(_.toString.contains("value"))
            .filter(_.toString.last < 58) // TODO: 判断是否为数值
        val indexes = jsonList.map(x => {
            val index = x.toString.split(" ")(0).split("\\.")(1)
            s"avg(" + index + s") as avg_$index"
        })

        println(indexes.mkString(","))

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

    }

}
