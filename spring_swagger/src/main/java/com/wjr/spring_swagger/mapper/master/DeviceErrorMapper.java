package com.wjr.spring_swagger.mapper.master;

import com.wjr.spring_swagger.bean.DwsDeviceManagement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 29375-wjr
 * @Package: com.wjr.spring_swagger.mapper.master
 * @ClassName: DeviceErrorMapper
 * @create 2022-03-30 18:49
 * @Description:
 */
@Mapper
public interface DeviceErrorMapper  {
    /**
     * 查询所有记录
     */
    List<DwsDeviceManagement> findAll();

}
