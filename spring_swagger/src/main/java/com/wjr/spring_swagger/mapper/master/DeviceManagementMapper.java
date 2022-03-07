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
    // 设备信息表
    List<DwsDeviceManagement> getDeviceManagements();
    List<DwsDeviceManagement> getRoadDeviceByName(String road_name);
    List<DwsDeviceManagement> getCityDeviceByName(String city_name);
    List<DwsDeviceManagement> getProvinceDeviceByName(String province_name);

    // 全国设备数量
    Long getNationDeviceCount();

    // 获取各地区传感器平均值
    List<DwsRegionDeviceValue> getRoadDeviceValueAvgByName(String road_name);
    List<DwsRegionDeviceValue> getCityDeviceValueAvgByName(String city_name);
    List<DwsRegionDeviceValue> getProvinceDeviceValueAvgByName(String province_name);
    // 获取地区的类型数
    List<AdsRegionTypeCount> getRegionTypeCount(String province_name);
}
