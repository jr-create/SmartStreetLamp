package com.wjr.spring_swagger.service.impl;

import com.wjr.spring_swagger.bean.DwsDeviceManagement;
import com.wjr.spring_swagger.mapper.master.DeviceErrorMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 29375-wjr
 * @Package: com.wjr.spring_swagger.service.impl
 * @ClassName: DeviceErrorServiceImpl
 * @create 2022-03-30 19:07
 * @Description:
 */
@Service
@Slf4j
public class DeviceErrorServiceImpl implements DeviceErrorMapper {
    @Autowired
    DeviceErrorMapper deviceErrorMapper;

    @Override
    public List<DwsDeviceManagement> findAll() {
        return deviceErrorMapper.findAll();
    }

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Autowired
    MessageHandleServiceImpl messageHandleService;

    @SneakyThrows
    @Bean
    public void getErrorDevice() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(this.getClass().getSimpleName()+"-- call schedule");
                List<DwsDeviceManagement> all = deviceErrorMapper.findAll();
                if (!ObjectUtils.isEmpty(all)) {//判断是否有异常设备
                    for (DwsDeviceManagement dwsDeviceManagement : all) {
                        if (!ObjectUtils.isEmpty(dwsDeviceManagement.getManager())) {//判断管理人员是否存在
                            String body = "道路：" + dwsDeviceManagement.getRoadName() + "，编号为" + dwsDeviceManagement.getDeviceId() + "路灯故障，" +
                                    dwsDeviceManagement.getManager() + "请去检查";
                            // messageHandleService.twilioSendSms("15735372489", body);//发送短信
                            messageHandleService.sendSimpleMail("智慧路灯维修",
                                    body, "247549095@qq.com");//发送邮箱
                            log.info("短信发送成功");
                            break;
                        }
                    }

                }
            }
        }, 20, 3 * 60, TimeUnit.SECONDS);

    }

}
