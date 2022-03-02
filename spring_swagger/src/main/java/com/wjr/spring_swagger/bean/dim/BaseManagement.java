package com.wjr.spring_swagger.bean.dim;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.dim
 * @ClassName: BaseManagement
 * @create 2022-03-01 21:28
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseManagement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    /**
     * id
     */
    private Integer id;

    /**
     * name
     */
    private String name;

    /**
     * road_id
     */
    private String roadName;

    /**
     * create_time
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @ApiModelProperty(example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = DATE_FORMAT)
    private Date startTime;

    /**
     * end_time
     */
    @ApiModelProperty(example = "2022-03-01 12:18:48")
    @JsonFormat(pattern = DATE_FORMAT)
    private Date endTime;

}
