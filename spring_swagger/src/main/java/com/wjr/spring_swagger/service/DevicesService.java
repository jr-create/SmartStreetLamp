
package com.wjr.spring_swagger.service;

import com.wjr.spring_swagger.bean.StmDevices;

import java.util.List;

public interface DevicesService {
    List<StmDevices> getAllDevice();
    void insertOrUpdateBatch(List<StmDevices> devicesList);
}
