package com.wjr.datasource;

import com.wjr.datasource.service.impl.MqttPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wjr.datasource")
public class DataSourceApplication {
    @Autowired
    static MqttPushService mqttPushService;
    public static void main(String[] args) {
        SpringApplication.run(DataSourceApplication.class, args);
        /**
         * 通配符
         * # 多层
         * + 单层任意
         */
        mqttPushService.subscribe("+/#");
    }

}
