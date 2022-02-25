package com.wjr.spark.utils

import com.wjr.spark.utils.decorator.JdbcSource

import java.sql.Connection

/**
 * @Package: com.wjr.spark.utils
 * @ClassName: ClickhouseJdbcUtil
 * @author Lenovo-wjr
 * @create 2022-02-06 18:40
 * @Description:
 */
object ClickhouseJdbcUtil extends JdbcSource {

    def getConnection(): Connection = {
        super.getSource("clickhouse").getConnection
    }

    def main(args: Array[String]): Unit = {
        println(ClickhouseJdbcUtil.queryList(getConnection(),
            """
              |select road_name,city_name,
              |               new_device_count,
              |               new_type_count,
              |               normal_count,
              |               abnormal_count,
              |               device_total,
              |               type_count,
              |               create_time
              |        from (
              |                 select city_name,
              |                        new_device_count,
              |                        new_type_count,
              |                        normal_count,
              |                        abnormal_count,
              |                        device_total,
              |                        type_count,
              |                        create_time,
              |                        row_number() over(partition by create_time order by create_time desc) as rk
              |                 from dws_device_management
              |                 where city_name = '海淀区'
              |             ) t1
              |        where rk = 1
              |""".stripMargin))
    }
}
