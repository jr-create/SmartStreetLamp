package com.wjr.spring_swagger.mapper.cluster;

import com.wjr.spring_swagger.bean.dim.BaseRoad;
import com.wjr.spring_swagger.mapper.base.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.boot.mapper
 * @ClassName: DevicesMapper
 * @create 2021-11-20 14:35
 * @Description:
 */
@Mapper
public interface BaseRoadMapper extends BaseMapper<BaseRoad> {
    /**
     * 根据属性
     */
    BaseRoad findBaseRoadByAttribute(String name, String cityName, Integer provinceId);

}
