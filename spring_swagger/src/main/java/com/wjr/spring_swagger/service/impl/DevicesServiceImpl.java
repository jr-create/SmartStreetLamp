package com.wjr.spring_swagger.service.impl;

import com.wjr.spring_swagger.bean.StmDevices;
import com.wjr.spring_swagger.mapper.cluster.DevicesMapper;
import com.wjr.spring_swagger.service.DevicesService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.boot.service.Impl
 * @ClassName: DevicesServiceImpl
 * @create 2021-11-20 14:34
 * @Description:
 */
@Service
public class DevicesServiceImpl implements DevicesService {

    private final DevicesMapper devicesMapper;

    public DevicesServiceImpl(DevicesMapper devicesMapper) {
        this.devicesMapper = devicesMapper;
    }

    @Override
    public List<StmDevices> getAllDevice() {
        return devicesMapper.getAllDevice();
    }
    @Override
    public void insertOrUpdateBatch(List<StmDevices> devicesList){
        devicesMapper.insertOrUpdateBatch(devicesList);
    }
}
