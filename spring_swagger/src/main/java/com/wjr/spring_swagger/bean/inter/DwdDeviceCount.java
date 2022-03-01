package com.wjr.spring_swagger.bean.inter;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwdDeviceCount
 * @create 2022-02-12 0:53
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DwdDeviceCount {

    /**
     * new_device_count
     */
    private String newDeviceCount;
    /**
     * new_type_count
     */
    private String newTypeCount;
    /**
     * device_total
     */
    private String deviceTotal;
    /**
     * normal_count
     */
    private String normalCount;
    /**
     * abnormal_count
     */
    private String abnormalCount;
    /**
     * type_count
     */
    private String typeCount;
    /**
     * create_time
     */
    private String createTime;

}
