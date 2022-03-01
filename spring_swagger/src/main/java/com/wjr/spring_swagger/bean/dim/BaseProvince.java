package com.wjr.spring_swagger.bean.dim;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.dim
 * @ClassName: BaseProvince
 * @create 2022-03-01 21:29
 * @Description:
 */
@Data
@AllArgsConstructor
public class BaseProvince implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    /**
     * id
     */
    private Integer id;

    /**
     * 省名称
     */
    private String name;

    /**
     * 大区id
     */
    private String regionId;

    /**
     * 行政区位码
     */
    private String areaCode;

    /**
     * 国际编码
     */
    private String isoCode;

    public BaseProvince() {}
}
