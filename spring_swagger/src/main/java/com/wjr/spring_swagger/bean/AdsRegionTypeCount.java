package com.wjr.spring_swagger.bean;

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
public class AdsRegionTypeCount {
    private String provinceName;
    private String typeId;
    private String typeName;
    private Long typeCount;
}
