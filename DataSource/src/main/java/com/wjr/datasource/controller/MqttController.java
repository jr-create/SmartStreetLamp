package com.wjr.datasource.controller;

import com.wjr.datasource.dao.MqttConfig;
import com.wjr.datasource.service.impl.MqttPublishClient;
import com.wjr.datasource.utils.JsonUtils;
import com.wjr.datasource.utils.LoggerAction;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 29375-wjr
 * @Package: com.wjr.datasource.controller
 * @ClassName: MqttController
 * @create 2022-05-05 10:15
 * @Description:
 */
@Controller
@RequestMapping("/mqtt")

public class MqttController {

    @Autowired
    MqttConfig mqttConfig;
    Logger logger = LoggerAction.logger;
    @GetMapping("/single")
    public String index() {
        logger.info(this.getClass().getSimpleName()+"-- get请求");
        return "mqtt";
    }

    @PostMapping("/index")
    @ResponseBody
    public ResponseEntity<String> postMsg(@RequestParam("topic") String topic, @RequestParam("msg") String msg) throws MqttException {

        System.out.println("post请求" + topic + " " + msg);
        try {
            MqttPublishClient mqttPushClient = new MqttPublishClient(
                    mqttConfig.getHostUrl(), mqttConfig.getUsername(), mqttConfig.getPassword(),
                    mqttConfig.getDefaultTopic());

            List<String> source = JsonUtils.getSource();
            for (int i = 0; i < source.size(); i++) {
                mqttPushClient.publish(topic, source.get(i));
            }
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("ERROR", HttpStatus.NO_CONTENT);
        }

    }

    ExecutorService executor = Executors.newFixedThreadPool(10);

    @GetMapping("/mult")
    @ResponseBody
    public ResponseEntity<String> postMsg() throws MqttException {
        try {
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    try {
                        MqttPublishClient mqttPushClient = new MqttPublishClient(
                                "tcp://124.220.2.188:1883", "", "",
                                "/topic/sensor");
                        List<String> source = JsonUtils.getSource();
                        for (int j = 0; j < source.size(); j++) {
                            Thread.sleep(1000L);
                            mqttPushClient.publish("topic" + j, source.get(j));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("ERROR", HttpStatus.NO_CONTENT);
        }

    }

}
