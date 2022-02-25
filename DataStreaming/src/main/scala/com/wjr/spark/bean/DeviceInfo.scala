package com.wjr.spark.bean

import org.apache.spark.sql.execution.streaming.FileStreamSource.Timestamp

import java.text.SimpleDateFormat


/**
 * 设备样例表
 */
case class DeviceInfo(
                         reason: String, //信息状态码
                         device_id: String, //设备id
                         type_id: Int, //设备类型
                         timestamp: Timestamp, //时间戳
                         error_code: Int, //错误码
                         road_id: Int, //路级id
                         longitude: String, //经度
                         latitude: String, //纬度
                         values: SensorValue //传感器数据
                     )
case class SensorValue(
                          voltage:Option[String],//电压
                          temperature:Option[String],//环境温度
                          humidity:Option[String],//湿度
                          lighting:Option[String],//光照强度
                          PM2_5:Option[String],//PM2.5含量
                          CO_2:Option[String],//Co2含量
                          power:Option[String],//风力
                      )
/**
 * 设备通用指标,存储在redis中缓存计算数量
 * @param devicee_id
 * @param type_id
 * @param error_code
 * @param road_id
 */
case class LampCurrencyIndex(
                               device_id:String,
                               road_id:Int,
                               type_id:Int,
                               longitude: String, //经度
                               latitude: String //纬度
                           )

case class Location(
                       road_id: String,
                       region_id: String,
                       province_id: String,
                       longitude: String,
                       latitude: String
                   )

/**
 * 传感器接口
 *
 * @param
 * @param env
 */
case class Sensor(
                     lamp: Option[LampSensor],
                     env: Option[EnvSensor]
                 )


/**
 * 路灯的指标
 *
 * @param id
 * @param voltage
 * @param status
 */
case class LampSensor(
                         id: String, //传感器id
                         voltage: String, //电压值
                         brightness: String, //路灯亮度
                         status: Int, //路灯状态
                         error_code: Int //错误码
                     )

/**
 * 环境监测传感器的指标
 *
 * @param id
 * @param temperature
 * @param humidity
 * @param lighting
 */
case class EnvSensor(
                        id: String, //传感器id
                        temperature: String, //温度值
                        humidity: String, //湿度值
                        lighting: String, //亮度值
                        error_code: Int //错误码
                    )

