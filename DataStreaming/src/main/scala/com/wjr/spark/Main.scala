package com.wjr.spark

import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.streaming.DeciceHandleFormatStatistics.DeciceHandleFormat
import com.wjr.spark.streaming.SourceDataCountClassification.SourceDataCount

/**
 * @Package: com.wjr.spark
 * @ClassName: Main
 * @author 29375-wjr
 * @create 2022-05-13 10:44
 * @Description:
 */
object Main {
  def main(args: Array[String]): Unit = {
    val spark = ProjectEnv.spark
    SourceDataCount(spark)
    DeciceHandleFormat(spark)
    //// 5、开启等待
    spark.streams.awaitAnyTermination()
  }

}
