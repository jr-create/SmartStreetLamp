package com.wjr.datasource.service.impl;

import com.wjr.datasource.utils.LoggerAction;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @Package: com.wjr.mqtt1.configuration
 * @ClassName: PushCallback
 * @Author: 86157
 * @CreateTime: 2021/10/5 20:06
 */
@Slf4j
public class PushCallback implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
        LoggerAction.logger.info("连接断开，可以做重连");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LoggerAction.logger.info("发布消息成功");
        LoggerAction.logger.info("接收消息主题 : " + topic);
        LoggerAction.logger.info("接收消息Qos : " + message.getQos());
        LoggerAction.logger.info("接收消息内容 : " + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //  TODO    此处可以将订阅得到的消息进行业务处理、数据存储
        try {
            LoggerAction.logger.info("deliveryComplete---------" + token.getMessage());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
