package com.wjr.spring_swagger.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.wjr.spring_swagger.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageHandleServiceImpl {


    /**
     * 通过 twilio 发送短信
     */
    public String  twilioSendSms(String mobile,String body){
        final String ACCOUNT_SID = "AC8149ab510d16d7a4e4f4119d54c79f28";
        final String AUTH_TOKEN = "ae341d6d76e1cc997a3a9c50f309eb60";
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber("+86"+mobile),
                        new com.twilio.type.PhoneNumber("+16068068106"),
                        body)
                .create();

        return message.getSid();
    }
    /**
     * 只能发送验证码格式
     * @param mobile
     */
    public void aliyunSendSms(String mobile,String code) {
        String host = "http://yzx.market.alicloudapi.com";
        String path = "/yzx/sendSms";
        String method = "POST";
        String appcode = "6501d20eee514ccdb643f5263d1e3c43";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        bodys.put("mobile", mobile);
        bodys.put("param", "code:"+code);
        bodys.put("tpl_id", "TP1710262");

        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
   public void sendInlineResourceMail(String topic,String receiver,String text,String rscId,String rscPath){
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
    }
}
