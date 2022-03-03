package com.wjr.spring_swagger.service.impl;

import com.wjr.spring_swagger.bean.AdsRegionTypeCount;
import com.wjr.spring_swagger.bean.DwsDeviceManagement;
import com.wjr.spring_swagger.bean.DwsRegionDeviceValue;
import com.wjr.spring_swagger.mapper.master.DeviceManagementMapper;
import com.wjr.spring_swagger.service.DeviceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.service.impl
 * @ClassName: DwsServiceImpl
 * @create 2022-02-21 22:18
 * @Description:
 */
@Service
public class DwsServiceImpl implements DeviceManagementService {

    @Autowired
    private  DeviceManagementMapper deviceManagementMapper;

    @Override
    public List<DwsDeviceManagement> getDeviceManagements() {
        return deviceManagementMapper.getDeviceManagements();
    }

    @Override
    public List<DwsDeviceManagement> getRoadDeviceByName(String road_name) {
        return deviceManagementMapper.getRoadDeviceByName(road_name);
    }

    @Override
    public List<DwsDeviceManagement> getCityDeviceByName(String city_name) {
        return deviceManagementMapper.getCityDeviceByName(city_name);
    }

    @Override
    public List<DwsDeviceManagement> getProvinceDeviceByName(String province_name) {
        return deviceManagementMapper.getProvinceDeviceByName(province_name);
    }

    @Override
    public List<DwsRegionDeviceValue> getRoadDeviceValueAvgByName(String road_name) {
        return deviceManagementMapper.getRoadDeviceValueAvgByName(road_name);
    }

    @Override
    public List<DwsRegionDeviceValue> getCityDeviceValueAvgByName(String city_name) {
        return deviceManagementMapper.getCityDeviceValueAvgByName(city_name);
    }

    @Override
    public List<DwsRegionDeviceValue> getProvinceDeviceValueAvgByName(String province_name) {
        return deviceManagementMapper.getProvinceDeviceValueAvgByName(province_name);
    }

    @Override
    public List<AdsRegionTypeCount> getRegionTypeCount() {
        return deviceManagementMapper.getRegionTypeCount();
    }
}
