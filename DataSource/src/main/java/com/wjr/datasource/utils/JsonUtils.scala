package com.wjr.datasource.utils

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object JsonUtils {

    def getSource: java.util.List[String] = {
        val arrayBuffer :ArrayBuffer[String] = ArrayBuffer()
        var json = ""

        for (i <- 0 until 100) {
            val random = new Random().nextInt(20 + 1) // TODO: 生成n+1的随机生成数
            val typeNum = new Random().nextInt(7 + 1) // TODO: 生成n+1的随机生成数
            val number = new Random().nextInt(9) + 1 // TODO: 生成n+1的随机生成数
            val device_id = new Random().nextInt(1000 + 1) // TODO: 生成n+1的随机生成数
            val error_code = 0 // TODO: 生成n+1的随机生成数
            val vol = Math.round((Random.nextGaussian()+4)*100)/100 // TODO: 生成n+1的随机生成数
            json =
                s"""
                   |{"reason":"success","device_id":"my_00$device_id","type_id":${typeNum},"timestamp":${System.currentTimeMillis() / 1000},"error_code":$error_code,"road_id":${random},"longitude":"11${number}.${number}820083778303","latitude":"3${number}.${number}242552469552","values":{"voltage":"$vol","temperature":"2${vol}","humidity":"${vol * 25}","lighting":"${vol * 25}","PM2_5":"${vol * 25}","CO_2":"${vol * 25}","info":"yin阴","direct":"xibeifeng西北风","power":"${number}级"},"test":["a1",2]}
                   |""".stripMargin
            arrayBuffer.append(json)
        }
        val strings = bufferAsJavaList(arrayBuffer)
        strings
    }


}
