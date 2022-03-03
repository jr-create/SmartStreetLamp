package com.wjr.spring_swagger.service.impl;

import com.wjr.spring_swagger.bean.dim.BaseDeviceType;
import com.wjr.spring_swagger.bean.dim.BaseManagement;
import com.wjr.spring_swagger.bean.dim.BaseProvince;
import com.wjr.spring_swagger.bean.dim.BaseRoad;
import com.wjr.spring_swagger.mapper.cluster.BaseDeviceTypeMapper;
import com.wjr.spring_swagger.mapper.cluster.BaseManagementMapper;
import com.wjr.spring_swagger.mapper.cluster.BaseProvinceMapper;
import com.wjr.spring_swagger.mapper.cluster.BaseRoadMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.boot.service.Impl
 * @ClassName: DevicesServiceImpl
 * @create 2021-11-20 14:34
 * @Description:
 */
@Service
public class LampServiceImpl {
    @Autowired
    BaseRoadMapper baseRoadMapper;

    @Autowired
    BaseProvinceMapper baseProvinceMapper;

    @Autowired
    BaseManagementMapper baseManagementMapper;

    @Autowired
    BaseDeviceTypeMapper baseDeviceTypeMapper;

    public List<BaseRoad> getAllDevice() {
        return baseRoadMapper.findAll();
    }

    public void insertBaseRoad(BaseRoad baseRoad) {
        baseRoadMapper.insert(baseRoad);
    }

    public void insertBaseManagement(BaseManagement baseManagement) {
        baseManagementMapper.insert(baseManagement);
    }

    public BaseProvince findBaseProvinceByName(String name) {
        return baseProvinceMapper.findProvinceByName(name);
    }

    public BaseRoad findBaseRoadByAttribute(String name, String cityName, Integer provinceId){
        return baseRoadMapper.findBaseRoadByAttribute(name,cityName,provinceId);
    }

    public List<BaseDeviceType> findAllBaseDeviceType(){
        return baseDeviceTypeMapper.findAll();
    }

}
