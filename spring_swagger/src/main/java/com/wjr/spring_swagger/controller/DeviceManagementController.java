package com.wjr.spring_swagger.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjr.spring_swagger.bean.AdsRegionTypeCount;
import com.wjr.spring_swagger.bean.dim.BaseDeviceType;
import com.wjr.spring_swagger.service.impl.DwsServiceImpl;
import com.wjr.spring_swagger.service.impl.LampServiceImpl;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.controller
 * @ClassName: DwsDeviceManagementController
 * @create 2022-02-21 23:12
 * @Description:
 */

@Slf4j
@RestController
@RequestMapping("/dws")
@Api(tags = "DwsDeviceManagement接口")
public class DeviceManagementController {

    @Autowired
    DwsServiceImpl dwsService;

    @Autowired
    LampServiceImpl lampService;

    @PostMapping("/getAllDeviceManagement")
    @ApiOperation(value = "查询", notes = "查询所有设备信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "road_name", value = "道路名称", paramType = "query"),
            @ApiImplicitParam(name = "city_name", value = "城市名称", paramType = "query"),
            @ApiImplicitParam(name = "province_name", value = "省份名称", paramType = "query"),
            @ApiImplicitParam(name = "page_num", value = "页数", defaultValue = "1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "data_num", value = "一页的条数", defaultValue = "100", paramType = "query", dataType = "int"),
    })
    public List getAllDeviceManagement(HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("page_num"));
        int dataNum = Integer.parseInt(request.getParameter("data_num"));
        String province_name = request.getParameter("province_name");
        String road_name = request.getParameter("road_name");
        String city_name = request.getParameter("city_name");
        List list = null;
        try {
            Page<?> page = PageHelper.startPage(pageNum, dataNum);
            log.info("设置第" + pageNum + "页两条数据!");
            if (road_name != null && !road_name.equals("null")) {
                list = dwsService.getRoadDeviceByName(road_name);
            } else if (city_name != null && !city_name.equals("null")) {
                list = dwsService.getCityDeviceByName(city_name);
            } else if (province_name != null && !province_name.equals("null")) {
                list = dwsService.getProvinceDeviceByName(province_name);
            } else {
                list = dwsService.getDeviceManagements();
            }
            log.info("总共有:" + page.getTotal() + "条数据,实际返回:" + list.size() + "两条数据!");
        } catch (Exception e) {
            log.info("查询" + this.getClass().getName() + "失败!原因是:" + e);
        }
        return list;
    }


    @PostMapping("/getRegionDeviceValueAvg")
    @ApiOperation(value = "查询", notes = "根据地区名称获取对应地区设备数据平均值信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "road_name", value = "道路名称", paramType = "query"),
            @ApiImplicitParam(name = "city_name", value = "城市名称", paramType = "query"),
            @ApiImplicitParam(name = "province_name", value = "省份名称", paramType = "query"),
    })
    public List getRegionDeviceValueAvg(HttpServletRequest request) {
        String province_name = request.getParameter("province_name");
        String road_name = request.getParameter("road_name");
        String city_name = request.getParameter("city_name");
        List devices = new ArrayList<>();
        if (road_name != null && !road_name.equals("null")) {
            devices = dwsService.getRoadDeviceValueAvgByName(road_name);
        } else if (city_name != null && !city_name.equals("null")) {
            devices = dwsService.getCityDeviceValueAvgByName(city_name);
        } else if (province_name != null && !province_name.equals("null")) {
            devices = dwsService.getProvinceDeviceValueAvgByName(province_name);
        }
        return devices;
    }


    @PostMapping("/getRegionTypeCount")
    @ApiOperation(value = "查询", notes = "获取各地区设备类型的数量")
    public List getRegionTypeCount(@ApiParam("province_name") @RequestParam String province_name) {
        // 获取地区设备类型数量
        List<AdsRegionTypeCount> regionTypeCount = dwsService.getRegionTypeCount(province_name);
        // 获取类型Id 对应的 类型名称
        List<BaseDeviceType> allBaseDeviceType = lampService.findAllBaseDeviceType();
        try {
            for (AdsRegionTypeCount adsRegionTypeCount : regionTypeCount) {
                String typeId = adsRegionTypeCount.getTypeId();
                // 将类型Id对应 类型名称
                Optional<BaseDeviceType> optionalBaseDeviceType = allBaseDeviceType.stream().filter(x -> x.getId() == Integer.parseInt(typeId)).findFirst();
                if (optionalBaseDeviceType.isPresent()) {
                    BaseDeviceType baseDeviceType = optionalBaseDeviceType.get();
                    adsRegionTypeCount.setTypeName(baseDeviceType.getName());
                } else {
                    adsRegionTypeCount.setTypeName(adsRegionTypeCount.getTypeId() + ":未定义");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regionTypeCount;
    }


    @PostMapping("/getNationDeviceCount")
    public String getNationDeviceCount(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","中国");
        jsonObject.put("count",dwsService.getNationDeviceCount());
        return jsonObject.toString();
    }
}
