package com.wjr.spring_swagger.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Service
public class MessageHandleServiceImpl {

    @Autowired
    private RestTemplate restTemplate;


    public String sendSms() {
        String url = "https://open.ucpaas.com/ol/sms/sendsms";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sid", "be4bb4a634e4ee04c336c35644a615b5");
        jsonObject.put("token", "d1a26dc811abffc81607729f3fad09f4");
        jsonObject.put("appid", "651f3cd3a973423792a303bc6d212f1d");
        jsonObject.put("templateid", "");
        jsonObject.put("param", "");
        jsonObject.put("mobile", "");
        jsonObject.put("uid", "");
        String json = JSONObject.toJSONString(jsonObject);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, httpHeaders);
        String result = restTemplate.postForObject(url, entity, String.class);

        return result;
    }

    @Autowired
    JavaMailSender javaMailSender;

    /**
     * 普通邮件发送
     */
    public String sendSimpleMail(String topic, String text, String... receiver) {
        // 构建一个邮件对象
        SimpleMailMessage message = new SimpleMailMessage();
        // 设置邮件主题
        message.setSubject(topic);
        // 设置邮件发送者，这个跟application.yml中设置的要一致
        message.setFrom("15735372489@163.com");
        // 设置邮件接收者，可以有多个接收者，中间用逗号隔开，以下类似
        // message.setTo("10*****16@qq.com","12****32*qq.com");
        message.setTo(receiver);
        // 设置邮件抄送人，可以有多个抄送人
        // message.setCc("15735372489@163.com");
        // 设置隐秘抄送人，可以有多个
        // message.setBcc("15735372489@163.com");
        // 设置邮件发送日期
        message.setSentDate(new Date());
        // 设置邮件的正文
        message.setText(text);
        // 发送邮件
        javaMailSender.send(message);
        return "success";
    }


    /**
     * 发送带附件的邮件
     */
/*    public void sendAttachFileMail() throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // true表示构建一个可以带附件的邮件对象
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);

        helper.setSubject("这是一封测试邮件");
        helper.setFrom("79******9@qq.com");
        helper.setTo("10*****16@qq.com");
        helper.setSentDate(new Date());
        helper.setText("这是测试邮件的正文");
        // 第一个参数是自定义的名称，后缀需要加上，第二个参数是文件的位置
        helper.addAttachment("资料.xlsx",new File("/Users/gamedev/Desktop/测试数据 2.xlsx"));
        javaMailSender.send(mimeMessage);

        // 案例二 ：测试富文本案例
        String rscId = "img110";
        String content = "<html><body><img width='250px' src=\'cid:"+ rscId+"\'></body></html>";
        sendInlineResourceMail("富文本测试案例","xxx@qq.com",content,rscId,"/xx/xx/path");
    }*/
    /**
     * 发送富文本邮件，支持文本、附件、HTML、图片
     * 如果需要发送多张图片，可以改变传参方式，使用集合添加多个<img src='cid:rscId'>和
     * helper.addInline(rscId, res);即可实现
     * @param topic 主题
     * @param receiver 接收者
     * @param text html内容
     * @param rscPath 文件路径
     */
/*    public void sendInlineResourceMail(String topic,String receiver,String text,String rscId,String rscPath){
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("");
            helper.setTo(receiver);
            helper.setSubject(topic);
            helper.setText(text,true);
            helper.setSentDate(new Date());
            File file = new File(rscPath);
            FileSystemResource res = new FileSystemResource(file);
            helper.addInline(rscId,res);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }*/
}
