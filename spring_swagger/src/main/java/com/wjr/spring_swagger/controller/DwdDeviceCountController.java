package com.wjr.spring_swagger.controller;

import com.wjr.spring_swagger.service.impl.DwdServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.controller
 * @ClassName: DwdController
 * @create 2022-02-13 18:45
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/dwd")
@Api(tags = "DwsDeviceCount接口")// TODO: 2022/1/29 标签组
public class DwdDeviceCountController {

    @Autowired
    DwdServiceImpl dwdService;

    @GetMapping("/getAllRoadDeviceCount")
    @ApiOperation(value = "查询", notes = "根据道路名称获取对应道路设备数量")
    public List getAllRoadDeviceCount() {
        return dwdService.getAllRoadDeviceCount();
    }

    @PostMapping("/getRoadCount")
    @ApiOperation(value = "查询", notes = "根据道路名称获取对应道路设备数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "road_name", value = "道路名称", required = true, paramType = "query"),
    })
    public List getRoadCount(HttpServletRequest request) {
        String road_name = request.getParameter("road_name");
        List devices = dwdService.getRoadDeviceCountById(road_name);
        return devices;
    }

    @PostMapping("/getCityCount")
    @ApiOperation(value = "查询", notes = "根据城市名称获取对应城市道路设备数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "city_name", value = "城市名称", required = true, paramType = "query"),
    })
    public List getCityCount(@RequestParam String city_name) {
        List devices = dwdService.getCityDeviceCountById(city_name);
        return devices;
    }

    @PostMapping("/getProvinceCount")
    @ApiOperation(value = "查询", notes = "根据省份名称获取对应省份设备数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "province_name", value = "省份名称", required = true, paramType = "query"),
    })
    public List getProvinceCount(@RequestParam String province_name) {
        List devices = dwdService.getProvinceDeviceCountById(province_name);
        return devices;
    }


    @PostMapping("/getAnyDeviceCount")
    @ApiOperation(value = "查询", notes = "根据省份名称获取对应省份设备数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "road_name", value = "道路名称", paramType = "query"),
            @ApiImplicitParam(name = "city_name", value = "城市名称", paramType = "query"),
            @ApiImplicitParam(name = "province_name", value = "省份名称", paramType = "query"),
    })
    public List getAnyDeviceCount(HttpServletRequest request) {
        String province_name = request.getParameter("province_name");
        String road_name = request.getParameter("road_name");
        String city_name = request.getParameter("city_name");
        List devices = new ArrayList<>();
        if (road_name != null && !road_name.equals("null")) {
            devices = dwdService.getRoadDeviceCountById(road_name);
        } else if (city_name != null && !city_name.equals("null")) {
            devices = dwdService.getCityDeviceCountById(city_name);
        } else if (province_name != null && !province_name.equals("null")) {
            devices = dwdService.getProvinceDeviceCountById(province_name);
        }
        return devices;
    }
}
