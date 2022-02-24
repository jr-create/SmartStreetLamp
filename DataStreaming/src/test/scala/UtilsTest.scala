import com.wjr.spark.sink.ClickhouseSink

/**
 * @Package:
 * @ClassName: UtilsTest
 * @author Lenovo-wjr
 * @create 2022-02-20 17:36
 * @Description:
 */
object UtilsTest extends App {
    //Expected one of: storage definition, ENGINE, AS (version 21.7.3.14 (official build))
    println(ClickhouseSink.prop.toString)
    println(ClickhouseSink.propertiesMap)
}
