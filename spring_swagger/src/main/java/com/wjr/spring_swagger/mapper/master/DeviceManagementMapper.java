package com.wjr.spring_swagger.mapper.master;

import com.wjr.spring_swagger.bean.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.mapper.master
 * @ClassName: DeviceManagement
 * @create 2022-02-21 22:23
 * @Description:
 */
@Mapper
public interface DeviceManagementMapper {
    List<DwsDeviceManagement> getAllDevice();
    List<DwsDeviceManagement> getRoadDeviceByName(String road_name);
    List<DwsDeviceManagement> getCityDeviceByName(String city_name);
    List<DwsDeviceManagement> getProvinceDeviceByName(String province_name);

    List<DwsRegionDeviceValue> getRoadDeviceValueAvgByName(String road_name);
    List<DwsRegionDeviceValue> getCityDeviceValueAvgByName(String city_name);
    List<DwsRegionDeviceValue> getProvinceDeviceValueAvgByName(String province_name);
    List<AdsRegionTypeCount> getRegionTypeCount();
}
