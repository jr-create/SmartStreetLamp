package com.wjr.spark.constant

/**
 * @Package: com.wjr.spark.constant
 * @ClassName: RedisConstant
 * @author Lenovo-wjr
 * @create 2022-02-17 0:48
 * @Description:
 */
object RedisConstant {
    final def dauDeviceNormalKey(road_id:String,dt:String) = s"dau:${road_id}:0:device:$dt" // TODO: 设备正常数，地区设备统计
    final def dauDeviceExceptKey(road_id:String,dt:String) = s"dau:${road_id}:1:device:$dt" // TODO: 设备异常数，地区设备统计
    final def dauTypeKey(road_id:String,dt:String) = s"dau:${road_id}:type:$dt" // TODO: 设备类型数，地区设备类型

}
