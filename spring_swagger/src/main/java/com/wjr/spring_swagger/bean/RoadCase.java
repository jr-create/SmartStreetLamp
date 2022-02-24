package com.wjr.spring_swagger.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("道路实体类")
public class RoadCase {

    // TODO: 2022/1/29 required = true 表示不为空
    @ApiModelProperty(value = "道路Id", required = true)
    private String road_id;
    @ApiModelProperty(value = "城市Id", required = true)
    private String city_id;
    @ApiModelProperty(value = "省级Id", required = true)
    private String provinc_id;
    @ApiModelProperty(value = "道路管理人员", required = true)
    private String manager_name;
}
