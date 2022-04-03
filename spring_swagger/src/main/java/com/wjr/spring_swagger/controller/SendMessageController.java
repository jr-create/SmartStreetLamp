package com.wjr.spring_swagger.controller;

import com.wjr.spring_swagger.bean.user.WebResult;
import com.wjr.spring_swagger.service.impl.MessageHandleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 29375-wjr
 * @Package: com.wjr.spring_swagger.controller
 * @ClassName: SendMessage
 * @create 2022-03-30 10:01
 * @Description:
 */
@Controller
@RequestMapping("/send")
public class SendMessageController {


    @Autowired
    MessageHandleServiceImpl messageHandleService;


    @RequestMapping("/bodySms")
    public WebResult bodySms(@RequestParam("mobile") String mobile,
                         @RequestParam("body") String body) {
        messageHandleService.twilioSendSms(mobile, body);
        return new WebResult(200, "success");
    }

    @RequestMapping("/codeSms")
    public WebResult codeSms(@RequestParam("mobile") String mobile,
                         @RequestParam("code") String code) {
        messageHandleService.aliyunSendSms(mobile, code);
        return new WebResult(200, "success");
    }

    @RequestMapping("/mail")
    public WebResult sms(@RequestParam("topic") String topic,
                         @RequestParam("text") String text,
                         @RequestParam("receiver") String... receiver) {
        messageHandleService.sendSimpleMail(topic, text, receiver);
        return new WebResult(200, "success");
    }
}
