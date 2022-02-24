package com.wjr.spring_swagger.bean;

import com.wjr.spring_swagger.bean.inter.DwdDeviceCount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwdAllDeviceCount
 * @create 2022-02-15 23:43
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DwsRoadManagement extends DwdDeviceCount {


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

}
