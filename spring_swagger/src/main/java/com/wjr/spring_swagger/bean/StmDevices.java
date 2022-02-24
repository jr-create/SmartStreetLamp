package com.wjr.spring_swagger.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
@TableName("devices")//实际指定数据库表
public class StmDevices {
    //设备id
    private String id;
    //设备名称
    private String name;
    //变量名称
    private String varName;
    //更新时间
    private Date updateTime;
    //当前数值
    private Float currentValue;

}
