package com.wjr.spring_swagger.controller;

import com.alibaba.fastjson.JSONObject;
import com.wjr.spring_swagger.bean.user.WebResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.controller
 * @ClassName: UserController
 * @create 2022-03-09 23:26
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/vue-element-admin/user")
@Api(tags = "用户接口")// TODO: 2022/1/29 标签组
public class UserController {

    @PostMapping("/login")
    @ApiOperation("登录")
    public WebResult login(HttpServletRequest request){
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        log.debug(username+" : "+password);
        try {
            log.debug(request.getReader().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug(request.getSession().toString());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token","admin");
        return new WebResult(20000,jsonObject);
    }

    @GetMapping("/info")
    @ApiOperation("登录")
    public WebResult info(HttpServletRequest request){
        log.debug(request.getSession().toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roles","admin");
        jsonObject.put("introduction","I am a super administrator");
        jsonObject.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        jsonObject.put("name","Super Admin");
        return new WebResult(20000,jsonObject);
    }
}
