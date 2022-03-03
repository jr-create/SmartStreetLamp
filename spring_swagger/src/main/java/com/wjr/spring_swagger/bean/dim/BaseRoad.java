package com.wjr.spring_swagger.bean.dim;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.boot.bean
 * @ClassName: StmDevices
 * @create 2021-11-20 14:26
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
@TableName("base_road")//实际指定数据库表
public class BaseRoad  {
    //设备id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //设备名称
    private String name;
    private String cityName;
    private Integer provinceId;
}
