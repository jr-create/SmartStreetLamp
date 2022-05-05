package com.wjr.datasource.service.impl;


import com.wjr.datasource.dao.MqttConfig;
import com.wjr.datasource.utils.LoggerAction;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

/**
 * @Package: com.wjr.mqtt1.configuration
 * @ClassName: MqttPushClient
 * @Author: 86157
 * @CreateTime: 2021/10/5 20:04
 */
@Slf4j
@Component
//@Scope("singleton")
public class MqttPushService {

    //为什么使用final spring配置默认的bean的scope是singleton，加上final只会在程序启动的时候初始化一次，并且在程序运行的时候不会再改变。
    public MqttConfig mqttConfig ;

    public MqttPushService(MqttConfig mqttConfig){
        this.mqttConfig = mqttConfig;
        connect();
    }

    private static final byte[] WILL_DATA;

    static {
        WILL_DATA = "offline".getBytes();
    }
    //Mqtt对象
    private static volatile MqttPushService mqttPushClient;

   // public static MqttPushService getInstance() throws MqttException {
   //
   //     if (null == mqttPushClient) {
   //         synchronized (MqttPushService.class) {
   //             if (null == mqttPushClient) {
   //                 mqttPushClient = new MqttPushService();
   //             }
   //         }
   //     }
   //     return mqttPushClient;
   // }


    private static MqttClient client;

    private void connect() {
        System.out.println("****"+mqttConfig.toString());
        String serverURI = mqttConfig.getHostUrl();

        String clientId = String.valueOf(System.currentTimeMillis());
        String userName = mqttConfig.getUsername();
        String passWord = mqttConfig.getPassword();

        //定义一个主题
        final String TOPIC = mqttConfig.getDefaultTopic();

        try {
            client = new MqttClient(serverURI, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，
            // 这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.toCharArray());
            // options.setServerURIs(StringUtils.split(HOST, ","));

            // 设置超时时间 单位为秒
            options.setConnectionTimeout(100);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置“遗嘱”消息的话题，若客户端与服务器之间的连接意外中断，服务器将发布客户端的“遗嘱”消息。
            options.setWill(TOPIC, WILL_DATA, 2, false);

            client.setCallback(new PushCallback());
            client.connect(options);
            LoggerAction.logger.info("连接成功！！！" + client);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布，默认qos为0，非持久化
     *
     * @param topic
     * @param pushMessage
     */
    public void publish(String topic, String pushMessage) {
        publish(0, false, topic, pushMessage);
    }

    /**
     * 发布
     *
     * @param qos
     * @param retained
     * @param topic
     * @param pushMessage
     */
    public void publish(int qos, boolean retained, String topic, String pushMessage) {
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());
        MqttTopic mTopic = client.getTopic(topic);//获取topic
        if (null == mTopic) {
            log.error("topic not exist");
        }
        MqttDeliveryToken token;
        try {
            token = mTopic.publish(message);
            token.waitForCompletion();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅某个主题，qos默认为0
     *
     * @param topic
     */
    public static void subscribe(String topic) {
        subscribe(topic, 0);
    }

    /**
     * 订阅某个主题
     *
     * @param topic
     * @param qos
     */
    public static void subscribe(String topic, int qos) {
        try {
            client.subscribe(topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
