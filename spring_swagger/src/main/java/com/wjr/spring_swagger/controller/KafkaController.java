package com.wjr.spring_swagger.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.controller
 * @ClassName: KafkaController
 * @create 2022-02-22 23:44
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/kafka")
public class KafkaController {

    // // //springboot提供的kafka支持
    // @Autowired
    // KafkaTemplate kafkaTemplate;//将kafka注入到Controller中
    //
    // // 消费监听
    // @PostMapping(value = "/getErrorMsg", produces = {"application/json;charset=UTF-8"})
    // @ResponseBody
    // @ApiOperation(value = "获取错误设备信息", notes = "获取错误设备信息")
    // @KafkaListener(topics = {"dws_device_error_handle"},groupId = "group_conusmer_http")
    // public String onKafkaMessage(ConsumerRecord<?, ?> record){
    //     // 消费的哪个topic、partition的消息,打印出消息内容
    //     String device_error_message = record.value().toString();
    //     System.out.println("简单消费："+record.topic()+"-"+record.partition()+"-"+ device_error_message);
    //     return device_error_message;
    // }
}
