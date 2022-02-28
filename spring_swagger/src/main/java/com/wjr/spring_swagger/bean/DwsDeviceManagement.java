package com.wjr.spring_swagger.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wjr.spring_swagger.bean.inter.DeviceInfo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.inter
 * @ClassName: DwsDeviceManagement
 * @create 2022-02-21 22:18
 * @Description:
 */

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


    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }
}
