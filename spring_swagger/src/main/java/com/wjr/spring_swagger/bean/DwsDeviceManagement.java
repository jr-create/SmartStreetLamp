package com.wjr.spring_swagger.bean;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.inter
 * @ClassName: DwsDeviceManagement
 * @create 2022-02-21 22:18
 * @Description:
 */

import com.wjr.spring_swagger.bean.inter.DwsValueAvg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 用户信息
 * @author zhengkai.blog.csdn.net
 * @date 2022-02-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DwsDeviceManagement extends DwsValueAvg {

    /**
     * reason
     */
    private String reason;

    /**
     * device_id
     */
    private String deviceId;

    /**
     * type_id
     */
    private Integer typeId;

    /**
     * timestamp
     */
    private Integer timestamp;

    /**
     * error_code
     */
    private Integer errorCode;

    /**
     * longitude
     */
    private String longitude;

    /**
     * latitude
     */
    private String latitude;

    /**
     * info
     */
    private String info;

    /**
     * direct
     */
    private String direct;

    /**
     * power
     */
    private String power;

    /**
     * road_id
     */
    private Integer roadId;

    /**
     * road_name
     */
    private String roadName;

    /**
     * manager
     */
    private String manager;

    /**
     * start_time
     */
    private String startTime;

    /**
     * end_time
     */
    private String endTime;

    /**
     * city_name
     */
    private String cityName;

    /**
     * province_name
     */
    private String provinceName;

    /**
     * area_code
     */
    private String areaCode;

    /**
     * iso_code
     */
    private String isoCode;

    /**
     * region_name
     */
    private String regionName;

    /**
     * dt
     */
    private String dt;

}
