package com.wjr.spring_swagger.service.impl;

import org.springframework.stereotype.Service;

/**
 * @author 29375-wjr
 * @Package: com.wjr.spring_swagger.service.impl
 * @ClassName: KafkaConsumerService
 * @create 2022-03-25 13:39
 * @Description:
 */
@Service
public class KafkaConsumerService {

    // //springboot提供的kafka支持
    // @Autowired
    // KafkaTemplate kafkaTemplate;//将kafka注入到Controller中
    //
    // // 消费监听
    // @KafkaListener(topics = {"wjr","ods_lamp_log"}, groupId = "group_conusmer_http")
    // public String onKafkaMessage(ConsumerRecord<?, ?> record) {
    //     // 消费的哪个topic、partition的消息,打印出消息内容
    //     String device_error_message = record.value().toString();
    //     System.out.println("简单消费：" + record.topic() + "-" + record.partition() + "-" + device_error_message);
    //     kafkaTemplate.send("wjr2","{data:123123}");
    //     return device_error_message;
    // }
}
