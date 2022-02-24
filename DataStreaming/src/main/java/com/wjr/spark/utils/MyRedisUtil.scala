package com.wjr.spark.utils

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
 *
 * @Package: com.wjr.gmall.realtime.utils
 * @ClassName: MyRedisUtil
 * @Author: 86157
 * @CreateTime: 2021/8/22 17:01
 */
object MyRedisUtil {
    var jedisPool : JedisPool = null

    //创建jedisPoll对象
    def build(): Unit ={
        val config = MyPropertiesUtil.load("config.properties")
        val host = config.getProperty("redis.host")
        val port = config.getProperty("redis.port")

        val jedisPoolConfig = new JedisPoolConfig()
        jedisPoolConfig.setMaxTotal(100) //最大连接数
        jedisPoolConfig.setMaxIdle(20) //最大空闲
        jedisPoolConfig.setMinIdle(20) //最小空闲
        jedisPoolConfig.setBlockWhenExhausted(true) //忙碌时是否等待
        jedisPoolConfig.setMaxWaitMillis(5000) //忙碌时等待时长 毫秒
        jedisPoolConfig.setTestOnBorrow(true) //每次获得连接的进行测试

        jedisPool = new JedisPool(jedisPoolConfig, host, port.toInt)
    }
    def getJedisClient : Jedis = {
        if (jedisPool == null) {
            build()
        }
        jedisPool.getResource
    }

    def main(args : Array[String]) : Unit = {
        val jedisClient = getJedisClient
        println(jedisClient.ping())
        jedisClient.close()
    }
}
