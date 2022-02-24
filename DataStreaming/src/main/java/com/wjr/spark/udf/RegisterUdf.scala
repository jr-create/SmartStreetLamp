package com.wjr.spark.udf

import com.wjr.spark.streaming.DeciceHandleFormatStatistics.spark
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

/**
 * @Package: com.wjr.spark.udf
 * @ClassName: jsonToArray
 * @author Lenovo-wjr
 * @create 2022-02-18 1:34
 * @Description:
 */
object RegisterUdf {
    def _json_array(json:String):Array[String]={
        implicit val formats = DefaultFormats

        val jValue = JsonMethods.parse(json)
        val array: Array[String] = jValue.extract[Array[String]]
        array
    }
}
