package com.wjr.spark.bean

import scala.beans.BeanProperty

/**
 * @Package: com.wjr.spark.bean
 * @ClassName: DeviceCountInfo
 * @author Lenovo-wjr
 * @create 2022-01-16 20:50
 * @Description: 设备数量样例类
 */

/**
 * 设备数量样例类
 */
case class DeviceCountInfo(
                        @BeanProperty      road_id: Int, //路级id

                        @BeanProperty      var new_device_count: Long,
                        @BeanProperty      var new_type_count: Long,
                        @BeanProperty      var normal_count: Long, //正常数
                        @BeanProperty      var abnormal_count: Long, //异常数
                        @BeanProperty      var device_total: Long, //设备总数
                        @BeanProperty      var type_count: Long, //设备类型数

                        @BeanProperty      var create_time: String //创建时间
                          )

case class DwsRoadManager(
                             road_id: Int, //路级id
                             var new_device_count: Long, //新增设备数
                             var new_type_count: Long, //新增类型数
                             var normal_count: Long, //正常数
                             var abnormal_count: Long, //异常数
                             var device_total: Long, //设备总数
                             var type_count: Long, //设备类型数
                             var create_time: String, //创建时间

                             var road_name: String, //路级名称
                             var management: String, //路级管理人员
                             var city_name: String, //市级名称
                             var province_name: String, //地区名
                             var province_area_code: String, //地区编码
                             var province_iso_code: String //国际地区编码
                         )

