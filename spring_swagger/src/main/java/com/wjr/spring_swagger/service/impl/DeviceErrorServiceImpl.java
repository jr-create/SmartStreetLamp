package com.wjr.spring_swagger.service.impl;

import com.wjr.spring_swagger.bean.DwsDeviceManagement;
import com.wjr.spring_swagger.mapper.master.DeviceErrorMapper;
import lombok.SneakyThrows;
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
                System.out.println("call schedule");
                List<DwsDeviceManagement> all = deviceErrorMapper.findAll();
                if (!ObjectUtils.isEmpty(all)) {
                    for (DwsDeviceManagement dwsDeviceManagement : all) {
                        if (!ObjectUtils.isEmpty(dwsDeviceManagement.getManager())){
                            String body = "道路：" + dwsDeviceManagement.getRoadName() + "，编号为" + dwsDeviceManagement.getRoadId() + "路灯故障，" +
                                    dwsDeviceManagement.getManager() + "请去检查";
                            messageHandleService.twilioSendSms("15735372489", body);
                            messageHandleService.sendSimpleMail("智慧路灯维修",
                                    body,"247549095@qq.com");
                            break;
                        }
                    }

                }
            }
        }, 20,5 * 60, TimeUnit.SECONDS);

    }

}
