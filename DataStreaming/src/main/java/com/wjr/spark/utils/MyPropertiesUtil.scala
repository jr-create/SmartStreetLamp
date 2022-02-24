package com.wjr.spark.utils

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

/**
 *
 * @Package:
 * @ClassName: MyPropertiesUtil
 * @Author: 86157
 * @CreateTime: 2021/8/22 16:33
 * @Desc: 读取配置文件
 */
object MyPropertiesUtil {

    /**
     * 加载配置文件
     *
     * @param propertiesName 配置文件路径
     * @return
     */
    def load(propertiesName : String) : Properties = {

        val properties = new Properties()
        //加载指定配置文件,文件是从classes中获取的，所以需要从当前线程的类加载器中获取
        properties.load(new InputStreamReader(
            Thread.currentThread().getContextClassLoader.getResourceAsStream(propertiesName),
            StandardCharsets.UTF_8))
        properties
    }

    def main(args : Array[String]) : Unit = {
        val properties = load("config.properties")
        val str = properties.getProperty("kafka.broker.list")
        println(str)
    }

}
