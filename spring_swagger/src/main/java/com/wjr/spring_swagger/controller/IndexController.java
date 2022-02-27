package com.wjr.spring_swagger.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@Api(tags = "Index接口")// TODO: 2022/1/29 标签组
public class IndexController {


    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:" + "/swagger-ui.html";
    }

}
