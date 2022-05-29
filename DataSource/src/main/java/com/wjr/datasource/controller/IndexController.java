package com.wjr.datasource.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wjr.datasource.utils.LoggerAction;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.datasource.controller
 * @ClassName: IndexController
 * @create 2021-12-26 11:13
 * @Description:
 */
@RestController
@RequestMapping("/kafka")
public class IndexController {

    //springboot提供的kafka支持
    @Autowired
    KafkaTemplate kafkaTemplate;//将kafka注入到Controller中

    Logger logger = LoggerAction.logger;
    @RequestMapping("/send")
    public String index(@RequestBody String mqttLog) {
        System.out.println("请求："+mqttLog);

        logger.info("请求："+mqttLog);
        JSONObject jsonObject = JSON.parseObject(mqttLog);
        JSONObject payload = jsonObject.getJSONObject("payload");
        logger.info(this.getClass().getSimpleName()+"--"+payload.toJSONString());

        kafkaTemplate.send("ods_lamp_log", payload.toJSONString());
        // Integer error_code = payload.getInteger("error_code");
        // if (error_code == 0) {//启动日志
        //     String topic = "smart_start_bak";
        //     System.out.println("mqtt_data send to "+topic);
        //     kafkaTemplate.send(topic, payload.toJSONString());//发送到kafka中
        // } else {//事件日志
        //     String topic = "smart_error_bak";
        //     kafkaTemplate.send(topic, payload.toJSONString());
        // }
        return mqttLog;
    }

    @RequestMapping("/test")
    public String test() {
        return "success";
    }
}
