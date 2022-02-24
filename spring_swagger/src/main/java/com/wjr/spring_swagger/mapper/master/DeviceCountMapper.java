package com.wjr.spring_swagger.mapper.master;

import com.wjr.spring_swagger.bean.DwsRegionDeviceCount;
import com.wjr.spring_swagger.bean.DwsRoadManagement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.mapper.master
 * @ClassName: DeviceCountMapper
 * @create 2022-02-12 21:22
 * @Description:
 */
@Mapper
public interface DeviceCountMapper  {
    List<DwsRegionDeviceCount> getRoadDeviceCountById(String road_name);
    List<DwsRegionDeviceCount> getCityDeviceCountById(String road_name);
    List<DwsRegionDeviceCount> getProvinceDeviceCountById(String province_name);
    List<DwsRoadManagement> getAllRoadDeviceCount();
}
