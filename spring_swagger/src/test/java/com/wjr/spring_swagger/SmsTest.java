package com.wjr.spring_swagger;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * @author 29375-wjr
 * @Package: com.wjr.spring_swagger
 * @ClassName: SmsTest
 * @create 2022-03-31 11:31
 * @Description:
 */
public class SmsTest {
    public static final String ACCOUNT_SID = "AC8149ab510d16d7a4e4f4119d54c79f28";
    public static final String AUTH_TOKEN = "ae341d6d76e1cc997a3a9c50f309eb60";

    public static void main(String[] args) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        String body = "道路：" + "学院路" + "，编号为" + 2 + "路灯故障，请" +
               "张三"+ "尽快去检查一下";
        Message message = Message.creator(
                        new PhoneNumber("+8615735372489"),
                        new PhoneNumber("+16068068106"),
                        "body")
                .create();

        System.out.println(message.getSid());
    }
}
