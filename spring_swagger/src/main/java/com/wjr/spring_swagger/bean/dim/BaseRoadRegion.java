package com.wjr.spring_swagger.bean.dim;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.dim
 * @ClassName: BaseRegion
 * @create 2022-03-12 22:22
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseRoadRegion {
    //设备id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //设备名称
    private String name;
    private String cityName;
    private String provinceName;
    private String regionName;
    private String areaCode;
    private String isoCode;
}
