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

    }

}
