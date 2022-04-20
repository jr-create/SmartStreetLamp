package com.wjr.flink.sql

import com.wjr.flink.stream.TempSensor
import org.apache.flink.api.scala.{ExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

import scala.util.Random

/**
 * @Package: com.wjr.flink.sql
 * @ClassName: TableTest
 * @author 29375-wjr
 * @create 2022-04-19 15:01
 * @Description:
 */
object TableTest {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)

    // 环境对比
    val oldSettings = EnvironmentSettings.newInstance().useOldPlanner().inStreamingMode().build()
    val oldStreamTableEnv = StreamTableEnvironment.create(env, oldSettings)
    // 老版本的批处理环境
    val batchEnv = ExecutionEnvironment.getExecutionEnvironment
    // val oldBatchTableEnv = BatchTableEnvironment.create(batchEnv)

    // 基于blink planner的流处理
    val blinkSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build()
    val blinkStreamTableEnv = StreamTableEnvironment.create(env, blinkSettings)
    // 基于blink planner的批处理
    val blinkBatchSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inBatchMode().build()
    val blinkBatchTableEnv = TableEnvironment.create(blinkBatchSettings)

    val inputFile = env.readTextFile("E:\\All_Project\\IDEA workspace\\SmartStreetLamp\\FlinkDemo\\src\\main\\resources\\wordCount.txt")

    val tempSource = inputFile.map(data => {
      val strings = data.split(" ")
      TempSensor(strings(0), System.currentTimeMillis(), Random.nextGaussian() + 20)
    })
    tempSource.print()
    // 首先创建表的执行环境
    val tableEnv = StreamTableEnvironment.create(env)
    // 基于流创建表
    val tempTable = tableEnv.fromDataStream(tempSource)
    // 调用table API进行转换
    val resultTable = tempTable.select($"id", $"temperature")
      .filter($"temperature" > 20) //需要加入import org.apache.flink.table.api._   用于隐式表达式转换
    // 将Table进行运行，并打印到本地客户端展示
    resultTable.execute().print()
    // 将Table类型转成DataStream，并展示
    tableEnv.toDataStream(resultTable).print()
    val dsRow = tableEnv.toAppendStream[Row](resultTable)
    val dsTuple = tableEnv.toAppendStream[(String, Int)](resultTable)

    // 直接使用sql
    tableEnv.createTemporaryView("tempTable", tempTable)
    val sql = "select * from tempTable"
    tableEnv.sqlQuery(sql).execute().print()
    // 只用流表

    env.execute("tableTest")
  }

}
