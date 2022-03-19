package com.wjr.spring_swagger.mapper.cluster;

import com.wjr.spring_swagger.bean.dim.BaseRoadRegion;
import com.wjr.spring_swagger.mapper.base.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.mapper.cluster
 * @ClassName: BaseRoadRegionMapper
 * @create 2022-03-12 22:24
 * @Description:
 */
@Mapper
public interface BaseRoadRegionMapper extends BaseMapper<BaseRoadRegion> {
    List<BaseRoadRegion> findByProvinceName(String id);
}
