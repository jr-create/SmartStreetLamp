package com.wjr.spring_swagger.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjr.spring_swagger.bean.dim.*;
import com.wjr.spring_swagger.mapper.cluster.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.boot.service.Impl
 * @ClassName: DevicesServiceImpl
 * @create 2021-11-20 14:34
 * @Description:
 */
@Service
@Slf4j
public class LampServiceImpl {
    @Autowired
    BaseRoadMapper baseRoadMapper;

    @Autowired
    BaseProvinceMapper baseProvinceMapper;

    @Autowired
    BaseManagementMapper baseManagementMapper;

    @Autowired
    BaseDeviceTypeMapper baseDeviceTypeMapper;

    @Autowired
    BaseRoadRegionMapper baseRoadRegionMapper;

    public List<BaseRoad> findAllDevice() {
        return baseRoadMapper.findAll();
    }

    public BaseRoad findByPrimaryKey(int id) {
        return baseRoadMapper.findByPrimaryKey(id);
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

    public BaseRoad findBaseRoadByAttribute(String name, String cityName, Integer provinceId) {
        return baseRoadMapper.findBaseRoadByAttribute(name, cityName, provinceId);
    }

    public List<BaseDeviceType> findAllBaseDeviceType() {
        return baseDeviceTypeMapper.findAll();
    }

    public List<BaseRoadRegion> getAllBaseRoadRegion() {
        return baseRoadRegionMapper.findAll();
    }

    public Object RoadList(int pageNum, int limitNum, String sort, Integer id, String provinceName) {
        JSONObject jsonObject = new JSONObject();
        Page<?> page = PageHelper.startPage(pageNum, limitNum);
        log.info("设置第" + pageNum + "页数据!");
        List<BaseRoadRegion> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(id) && ObjectUtils.isEmpty(provinceName)) {
            list = getAllBaseRoadRegion();
        }
        jsonObject.put("total", page.getTotal());
        if (!ObjectUtils.isEmpty(sort) && sort.equals("-id")) {//倒序
            Collections.reverse(list);
        }
        if (!ObjectUtils.isEmpty(id)) { // 根据Id进行查询
            BaseRoadRegion byPrimaryKey = baseRoadRegionMapper.findByPrimaryKey(id);
            list.clear();
            list.add(byPrimaryKey);
        }
        if (!ObjectUtils.isEmpty(provinceName)) {// 根据省份名称进行查询
            list.clear();
            list = baseRoadRegionMapper.findByProvinceName(provinceName);
        }
        jsonObject.put("items", list);
        log.info("总共有:" + page.getTotal() + "条数据,实际返回:" + list.size() + "条数据!");
        return jsonObject;
    }

    public Object findAllProvinceName() {
        return baseProvinceMapper.findAll();
    }

    public Object findAllBaseManagement(int pageNum, int limitNum){
        JSONObject jsonObject = new JSONObject();
        Page<?> page = PageHelper.startPage(pageNum, limitNum);
        log.info("设置第" + pageNum + "页数据!");
        List<BaseManagement> managements = baseManagementMapper.findAll();
        jsonObject.put("total", page.getTotal());
        jsonObject.put("items", managements);
        log.info("总共有:" + page.getTotal() + "条数据,实际返回:" + managements.size() + "条数据!");
        return jsonObject;
    }
}
