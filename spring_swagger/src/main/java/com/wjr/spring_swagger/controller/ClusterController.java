package com.wjr.spring_swagger.controller;

import com.wjr.spring_swagger.bean.dim.BaseManagement;
import com.wjr.spring_swagger.bean.dim.BaseProvince;
import com.wjr.spring_swagger.bean.dim.BaseRoad;
import com.wjr.spring_swagger.bean.user.WebResult;
import com.wjr.spring_swagger.service.impl.LampServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.controller
 * @ClassName: ClusterController
 * @create 2022-02-24 22:27
 * @Description:
 */
@Slf4j
@RestController
@Api(tags = "MySQL接口", value = "对维度表进行操作")
@RequestMapping("vue-element-admin/article")
public class ClusterController {


    @Autowired
    LampServiceImpl lampService;

    @GetMapping("RoadList")
    @ApiOperation(value = "查询", notes = "查询Road")
    public WebResult RoadList(@RequestParam("page") int pageNum,
                              @RequestParam("limit") int limitNum,
                              @RequestParam(value = "sort", required = false) String sort,
                              @RequestParam(value = "id", required = false) Integer id,
                              @RequestParam(value = "provinceName", required = false) String provinceName
    ) {
        return new WebResult(20000, lampService.RoadList(pageNum, limitNum, sort, id, provinceName));
    }

    @GetMapping("getAllProvinceName")
    public WebResult getAllProvinceName() {
        return new WebResult(20000, lampService.getAllProvinceName());
    }

    @PostMapping("addBaseRoad")
    @ApiOperation(value = "插入", notes = "添加道路信息")
    public WebResult addBaseRoad(
            @RequestBody Map<String, String> request
    ) {
        request.entrySet().forEach(x -> log.info(x.getKey() + " " + x.getValue()));
        String msg;
        String provinceName = request.getOrDefault("provinceName", null);
        String cityName = request.getOrDefault("cityName", null);
        String roadName = request.getOrDefault("roadName", null);
        log.info(provinceName + " " + cityName + " " + roadName);
        if (ObjectUtils.isEmpty(provinceName) || ObjectUtils.isEmpty(cityName) || ObjectUtils.isEmpty(roadName)) {
            return new WebResult(30000, "error");
        }
        BaseProvince province = lampService.findBaseProvinceByName(provinceName);
        Integer provinceId = province.getId();
        if (ObjectUtils.isEmpty(lampService.findBaseRoadByAttribute(roadName, cityName, provinceId))) {
            lampService.insertBaseRoad(new BaseRoad(1, roadName, cityName, provinceId));
            msg = "success";
        } else {
            msg = "道路已经存在";
            return new WebResult(40000, msg);
        }
        return new WebResult(20000, msg);
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
    public WebResult addManagement(@RequestParam String name,
                                   @RequestParam String road_name,
                                   Date start_time,
                                   Date end_time) {

        if (ObjectUtils.isEmpty(start_time) || ObjectUtils.isEmpty(end_time)) {
            try {
                start_time = new Date();
                end_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("9999-12-31 23:59:59");
            } catch (Exception e) {
                e.printStackTrace();
                return new WebResult(20000, "error");
            }
        }
        BaseManagement baseManagement = new BaseManagement(1, name, road_name, start_time, end_time);
        lampService.insertBaseManagement(baseManagement);
        return new WebResult(20000, "success");
    }
}
