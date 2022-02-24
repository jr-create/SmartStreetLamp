package com.wjr.spring_swagger.controller;

import com.wjr.spring_swagger.bean.RoadCase;
import com.wjr.spring_swagger.bean.StmDevices;
import com.wjr.spring_swagger.service.impl.DevicesServiceImpl;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@Api(tags = "Index接口")// TODO: 2022/1/29 标签组
public class IndexController {

    public IndexController(DevicesServiceImpl devicesService) {
        this.devicesService = devicesService;
    }

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:" + "/swagger-ui.html";
    }

    @PostMapping
    @ResponseBody
    @ApiOperation(value = "导入道路信息", notes = "")// TODO: 2022/1/29 接口标题信息
    public RoadCase regionTable(@ApiParam() RoadCase roadCase) {
        return roadCase;
    }

    @GetMapping("/query")
    @ResponseBody
    @ApiOperation(value = "查询", notes = "根据road_id查询")// TODO: 2022/1/29 接口标题信息
    @ApiImplicitParams({
            /**
             * required=true:表示字段不为空
             *  paramType：参数放在哪个地方
             *             · header --> 请求参数的获取：@RequestHeader
             *             · query --> 请求参数的获取：@RequestParam
             *             · path（用于restful接口）--> 请求参数的获取：@PathVariable
             *             · body（不常用）
             *             · form（不常用）
             * dataType：参数类型，默认String，其它值dataType="Integer"
             * defaultValue：参数的默认值
             */
            @ApiImplicitParam(name = "road_id", value = "路级Id", required = true, paramType = "query")
    })
    public String query(@RequestParam String road_id) {
        return road_id;
    }



    @GetMapping("/getDeviceCount")
    @ResponseBody
    @ApiOperation(value = "查询", notes = "通过路级Id查询设备数量表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "road_id", value = "路级Id", required = true, paramType = "query"),
    })
    public List getDeviceCount(@RequestParam String road_id) {
        ArrayList<String> list = new ArrayList<>();
        return list;
    }

    final DevicesServiceImpl devicesService;

    @GetMapping("/testMysql")
    @ResponseBody
    @ApiOperation(value = "查询", notes = "获取所有设备")
    public List editable_table() {
        List<StmDevices> devices = devicesService.getAllDevice();
        return devices;
    }

}
