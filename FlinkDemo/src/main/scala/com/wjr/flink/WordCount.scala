package com.wjr.flink

import org.apache.flink.api.scala.{ExecutionEnvironment, createTypeInformation}

object WordCount {
    val path = "E:\\All_Project\\IDEA workspace\\SmartStreetLamp\\FlinkDemo\\src\\main\\resources\\wordCount.txt"

    def main(args: Array[String]): Unit = {
        val env = ExecutionEnvironment.getExecutionEnvironment
        val lines = env.readTextFile(path)
        val wordCount = lines.flatMap(_.split(" ")).map((_, 1)).groupBy(0).sum(1)
        wordCount.print()
    }
}
