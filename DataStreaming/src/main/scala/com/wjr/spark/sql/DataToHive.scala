package com.wjr.spark.sql

import com.wjr.spark.env.ProjectEnv

/**
 * @Package: com.wjr.spark.sql
 * @ClassName: DataToHive
 * @author Lenovo-wjr
 * @create 2022-02-17 23:33
 * @Description:
 */
object DataToHive {
    def main(args: Array[String]): Unit = {
        val spark = ProjectEnv.spark
        spark.sql(
            """
              |create EXTERNAL table  if not exists  lamp.dwd_device_index(
              |reason string,device_id string,type_id int,
              |`timestamp` int,error_code int,
              |longitude string,latitude string,
              |test array<string>,voltage string,
              |temperature string,humidity string,
              |lighting string,PM2_5 string,CO_2 string,
              |info string,direct string,power string
              |)PARTITIONED BY (`dt` string,road_id string)
              |STORED As parquet
              |LOCATION 'hdfs://hadoop01:8020/warehouse/smartStreetLamp/dwd/dwd_device_index'
              |""".stripMargin).show()
        spark.sql(
            """
              |insert into lamp.dwd_device_index partition(dt="2022-02-12" ,road_id="1")
              |values( 'success','my_00889',18,1645122103,1,'116.38200838778303','39.92425852469552',array('a1','2'),'1','2.5','25','25','25','25','yin阴','xibeifeng西北风','8级');
              |""".stripMargin).show()
        spark.sql(
            """
              |select * from lamp.dwd_device_index
              |""".stripMargin).show(100,false)
        spark.close()
    }

}
