package com.wjr.spring_swagger.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wjr.spring_swagger.bean.inter.DeviceValues;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.inter
 * @ClassName: DwsRoadDeviceValue
 * @create 2022-02-21 22:53
 * @Description:
 */
@Data
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
@AllArgsConstructor
@NoArgsConstructor
public class DwsRegionDeviceValue extends DeviceValues {

    private String roadName;
    private String provinceName;
    private String cityName;

}
