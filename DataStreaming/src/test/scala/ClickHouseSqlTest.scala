import com.fasterxml.jackson.databind.ObjectMapper
import com.wjr.spark.bean.{DeviceCountInfo, DeviceInfo}
import com.wjr.spark.env.ProjectEnv
import com.wjr.spark.utils.{JsonUtils, SqlUtils}
import org.json4s.jackson.JsonMethods

import scala.util.Random

object ClickHouseSqlTest {
    def main(args: Array[String]): Unit = {

        val random = new Random().nextInt(11 + 1) // TODO: 生成n+1的随机生成数
        val number = new Random().nextInt(9) + 1 // TODO: 生成n+1的随机生成数
        val device_id = new Random().nextInt(1000 + 1) // TODO: 生成n+1的随机生成数
        val error_code = 0 // TODO: 生成n+1的随机生成数
        val vol = Math.random()*(5-1)+1 // TODO: 生成n+1的随机生成数
        val json =
            s"""
               |{"reason":"success","device_id":"my_00$device_id","type_id":${random},"timestamp":${System.currentTimeMillis() / 1000},"error_code":$error_code,"road_id":${random},"longitude":"11${number}.${number}820083778303","latitude":"3${number}.${number}242552469552","values":{"voltage":"$vol","temperature":"${vol * 2.5}","humidity":"${vol * 25}","lighting":"${vol * 25}","PM2_5":"${vol * 25}","CO_2":"${vol * 25}","info":"yin阴","direct":"xibeifeng西北风","power":"${number}级"},"test":["a1",2]}
               |""".stripMargin


        val buffer2 = JsonUtils.jsonToList(json).filter(!_.toString.contains("test")).map(x=>{
            val value = x.toString.split(" ")
            (value(0).split("\\.").last,value(1),value(2))
        })
        println(buffer2)
        println(SqlUtils.createCKTable("dwd_error_device", buffer2.map(x => (x._1, x._2)).toMap, "MergeTree", "timestamp", "device_id", "timestamp"))
        println(SqlUtils.insertSql("dwd_error_device", buffer2.map(_._3).toBuffer))

    }

}
