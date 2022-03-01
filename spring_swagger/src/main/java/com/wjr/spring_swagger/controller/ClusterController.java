package com.wjr.spring_swagger.controller;

import com.wjr.spring_swagger.bean.dim.BaseManagement;
import com.wjr.spring_swagger.bean.dim.BaseProvince;
import com.wjr.spring_swagger.bean.dim.BaseRoad;
import com.wjr.spring_swagger.service.impl.LampServiceImpl;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.controller
 * @ClassName: ClusterController
 * @create 2022-02-24 22:27
 * @Description:
 */
@Slf4j
@Controller
@Api(tags = "MySQL接口", value = "对维度表进行操作")
public class ClusterController {


    @Autowired
    LampServiceImpl lampService;

    @GetMapping("selectRoad")
    @ResponseBody
    @ApiOperation(value = "查询", notes = "查询Road")
    public List selectRoad() {
        List<BaseRoad> list = lampService.getAllDevice();
        return list;
    }

    @PostMapping("addRoad")
    @ResponseBody
    @ApiOperation(value = "插入", notes = "添加道路信息")
    public String addRoad(
            @ApiParam(name = "province_name", value = "省份名称") @RequestParam String province_name,
            @ApiParam(name = "city_name", value = "市级名称") @RequestParam String city_name,
            @ApiParam(name = "name", value = "道路名称") @RequestParam String name
    ) {
        String msg;
        BaseProvince province = lampService.findBaseProvinceByName(province_name);
        Integer provinceId = province.getId();
        if (ObjectUtils.isEmpty(lampService.findBaseRoadByAttribute(name, city_name, provinceId))) {
            lampService.insertBaseRoad(new BaseRoad(1, name, city_name, provinceId));
            msg = "插入成功";
        } else {
            msg = "道路已经存在";
        }
        return msg;
    }

    @PostMapping("addManagement")
    @ResponseBody
    @ApiOperation(value = "插入", notes = "添加管理人员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "管理人员", required = true),
            @ApiImplicitParam(name = "road_name", value = "道路名称", required = true),
            @ApiImplicitParam(name = "start_time", value = "开始时间"),
            @ApiImplicitParam(name = "end_time", value = "结束时间")
    })
    public String addManagement(@RequestParam String name,
                                @RequestParam String road_name,
                                Date start_time,
                                Date end_time) {

        if (ObjectUtils.isEmpty(start_time) || ObjectUtils.isEmpty(end_time)) {
            try {
                start_time = new Date();
                end_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("9999-12-31 23:59:59");
            } catch (Exception e) {
                e.printStackTrace();
                return "插入失败";
            }
        }
        BaseManagement baseManagement = new BaseManagement(1, name, road_name, start_time, end_time);
        lampService.insertBaseManagement(baseManagement);
        return "插入成功";
    }
}
