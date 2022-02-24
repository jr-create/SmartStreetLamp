package com.wjr.spring_swagger.service.impl;

import com.wjr.spring_swagger.bean.DwsRoadManagement;
import com.wjr.spring_swagger.mapper.master.DeviceCountMapper;
import com.wjr.spring_swagger.service.DwdServeice;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.service.impl
 * @ClassName: DeviceCountMapper
 * @create 2022-02-12 21:24
 * @Description:
 */
@Service
public class DwdServiceImpl implements DwdServeice {

    private final DeviceCountMapper deviceCountMapper;
    public DwdServiceImpl(DeviceCountMapper deviceCountMapper) {
        this.deviceCountMapper = deviceCountMapper;
    }

    @Override
    public List getRoadDeviceCountById(String road_name) {
        return deviceCountMapper.getRoadDeviceCountById(road_name);
    }

    @Override
    public List getCityDeviceCountById(String road_name) {
        return deviceCountMapper.getCityDeviceCountById(road_name);
    }

    @Override
    public List getProvinceDeviceCountById(String province_name) {
        return deviceCountMapper.getProvinceDeviceCountById(province_name);
    }

    @Override
    public List<DwsRoadManagement> getAllRoadDeviceCount() {
        return deviceCountMapper.getAllRoadDeviceCount();
    }
}
