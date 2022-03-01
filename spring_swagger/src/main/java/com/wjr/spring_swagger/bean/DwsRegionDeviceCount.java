package com.wjr.spring_swagger.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wjr.spring_swagger.bean.inter.DwdDeviceCount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwdProvinceDeviceCount
 * @create 2022-02-15 20:45
 * @Description:
 */
@Data
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
@AllArgsConstructor
@NoArgsConstructor
public class DwsRegionDeviceCount extends DwdDeviceCount {

    private String roadName;
    private String provinceName;
    private String cityName;

}
