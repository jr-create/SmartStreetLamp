package com.wjr.spring_swagger.bean.inter;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwdDeviceCount
 * @create 2022-02-12 0:53
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 设备数量
 * @author zhengkai.blog.csdn.net
 * @date 2022-02-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// @TableName("dwd_device_count")//实际指定数据库表
public class DwdDeviceCount {

    /**
     * new_device_count
     */
    private String newDeviceCount;
    /**
     * new_type_count
     */
    private String newTypeCount;
    /**
     * device_total
     */
    private String deviceTotal;
    /**
     * normal_count
     */
    private String normalCount;
    /**
     * abnormal_count
     */
    private String abnormalCount;
    /**
     * type_count
     */
    private String typeCount;
    /**
     * create_time
     */
    private String createTime;

}
