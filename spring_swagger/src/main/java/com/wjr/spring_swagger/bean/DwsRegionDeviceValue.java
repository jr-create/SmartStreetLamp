package com.wjr.spring_swagger.bean;

import com.wjr.spring_swagger.bean.inter.DwsValueAvg;
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
@AllArgsConstructor
@NoArgsConstructor
public class DwsRegionDeviceValue extends DwsValueAvg {

    private String roadName;
    private String provinceName;
    private String cityName;
}
