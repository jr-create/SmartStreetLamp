package com.wjr.spring_swagger.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wjr.spring_swagger.bean.inter.DwdDeviceCount;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwdProvinceDeviceCount
 * @create 2022-02-15 20:45
 * @Description:
 */
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
@AllArgsConstructor
@NoArgsConstructor
public class DwsRegionDeviceCount extends DwdDeviceCount {

    private String roadName;
    private String provinceName;
    private String cityName;

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
