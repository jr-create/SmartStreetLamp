package com.wjr.datasource.dao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @Package: com.wjr.mqtt_web.configuration
 * @ClassName: MqttConfig
 * @Author: 86157
 * @CreateTime: 2021/10/6 22:44
 */
@Data
// @Repository //dao层
@Component
@ConfigurationProperties("spring.mqtt")//文件中，后面不能使用#注释

//方式二：文件一
// @ConfigurationProperties(prefix="spring.mqtt")
// @PropertySource("classpath:application.yml")//文件中，后面不能使用#注释

//方式二：文件二
//@ConfigurationProperties(prefix="mqtt")
//@PropertySource("classpath:mqtt.properties")
public class MqttConfig {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 连接地址
     */
    private String hostUrl;
    /**
     * 客户Id
     */
    private String clientId;
    /**
     * 默认连接话题
     */
    private String defaultTopic;
    /**
     * 超时时间
     */
    private int timeout;
    /**
     * 保持连接数
     */
    private int keepalive;
}
