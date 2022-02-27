package com.wjr.spring_swagger.mapper.cluster;

import com.wjr.spring_swagger.bean.BaseRoad;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.boot.mapper
 * @ClassName: DevicesMapper
 * @create 2021-11-20 14:35
 * @Description:
 */
@Mapper
public interface DevicesMapper   {
    List<BaseRoad> getAllDevice();
}
