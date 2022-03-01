package com.wjr.spring_swagger.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wjr.spring_swagger.bean.inter.DeviceInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.inter
 * @ClassName: DwsDeviceManagement
 * @create 2022-02-21 22:18
 * @Description:
 */
@Data
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
@AllArgsConstructor
@NoArgsConstructor
public class DwsDeviceManagement extends DeviceInfo {

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
