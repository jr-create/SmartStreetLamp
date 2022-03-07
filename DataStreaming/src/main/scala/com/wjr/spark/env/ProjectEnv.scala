package com.wjr.spark.env

import com.wjr.spark.udf.RegisterUdf
import org.apache.log4j.spi.LoggerFactory
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.StreamingQueryListener
import org.apache.spark.sql.streaming.StreamingQueryListener.{QueryProgressEvent, QueryStartedEvent, QueryTerminatedEvent}

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

case class Record(key: Int, value: String)

object ProjectEnv {
    final val appName = "Spark"
    private[spark] final val spark: SparkSession = SparkSession.builder
        .config("spark.sql.parquet.writeLegacyFormat", true)
        // TODO: 序列化保证
        .master("local[*]").appName(this.appName)
        .enableHiveSupport()
        //.withExtensions todo Catalyst优化
        //{ extensions =>
        //    extensions.injectOptimizerRule { session => PushDownPartitionPredicate(session) }
        //    extensions.injectOptimizerRule { session => JoinOptimizer(session) }
        //    extensions.injectOptimizerRule { session => SampleExecution(session) }
        //    extensions.injectPlannerStrategy{ sesson => HiveTableWithFilterScans(sesson)}
        //}
        .getOrCreate()
    /**
     * Spark配置
     */
    val sparkContext: SparkContext = spark.sparkContext
    /**
     * Structured Streaming 监控任务
     */
    val listener = new StreamingQueryListener {
        private val streamingIdMap = new ConcurrentHashMap[UUID, String]

        override def onQueryStarted(event: QueryStartedEvent): Unit = {
            if (event.id != null && event.name != null) {
                streamingIdMap.put(event.id, event.name)
            }
            //           listenerBus.postEvent(StreamingStarted(event.name))
            println("Query started: " + event.id)
        }

        override def onQueryProgress(event: QueryProgressEvent): Unit = {
            //println("Query made progress: " + event.progress)
            if (event.progress.numInputRows > 0) {
                println(s"[StreamingQueryListener] name: ${event.progress.name}," +
                    s" numInputRows: ${event.progress.numInputRows}")
            }
        }

        override def onQueryTerminated(event: QueryTerminatedEvent): Unit = {
            val streamingId = streamingIdMap.remove(event.id)
            //listenerBus.postEvent(StreamingStoped(streamingId, event.exception))
            println(streamingId)
            println("Query terminated: " + event.id)
        }
    }

    /**
     * 元数据缓存
     */
    var json :String =
        """
          |{"reason":"success","device_id":"my_00721","type_id":6,"timestamp":1645114771,"error_code":0,"road_id":6,"longitude":"116.38200833778303","latitude":"39.92425352469552","values":{"voltage":"0","temperature":"0.0","humidity":"0","lighting":"0","PM2_5":"0","CO_2":"0","info":"yin阴","direct":"xibeifeng西北风","power":"3级"},"test":["a1",2]}
          |""".stripMargin

    def instance( json:String = ""): String = synchronized {
        if (json != null) {
            this.json = json
        }
        this.json
    }


    def init(): Unit = {
        // TODO: 配置hive动态分区
        spark.sqlContext.setConf("hive.exec.dynamic.partition", "true")
        spark.sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")
        // TODO: 配置hive小文件合并
        spark.sqlContext.setConf("hive.merge.smallfiles.avgsize", "100000000")
        spark.sqlContext.setConf("hive.exec.max.dynamic.partitions", "2048")
        spark.sqlContext.setConf("hive.exec.max.dynamic.partitions.pernode", "256")
        // TODO: 设置spark log格式
        spark.sparkContext.setLogLevel("WARN")
        // TODO: 添加监听事件
        spark.streams.addListener(listener)
        // TODO: 注册udf函数
        spark.udf.register("get_json_array", RegisterUdf._json_array(_))
        //spark.udf.register("get_json_array", (json: String) => {
        //    implicit val formats = DefaultFormats
        //
        //    val jValue = JsonMethods.parse(json)
        //    val array: Array[String] = jValue.extract[Array[String]]
        //    array
        //})
    }

    init
}

trait Logging extends LoggerFactory {


}
