package com.wjr.spring_swagger.bean.dim;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.inter
 * @ClassName: DwsDeviceManagement
 * @create 2022-03-02 15:52:59
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseDeviceType {
    @TableId(type = IdType.AUTO)
    /**
     * id
     */
    private Integer id;
    private String typeName;
    private String func;
}
