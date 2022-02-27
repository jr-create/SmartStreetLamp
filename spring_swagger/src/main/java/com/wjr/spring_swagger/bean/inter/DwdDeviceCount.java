package com.wjr.spring_swagger.bean.inter;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwdDeviceCount
 * @create 2022-02-12 0:53
 * @Description:
 */

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

    public String getNewDeviceCount() {
        return newDeviceCount;
    }

    public void setNewDeviceCount(String newDeviceCount) {
        this.newDeviceCount = newDeviceCount;
    }

    public String getNewTypeCount() {
        return newTypeCount;
    }

    public void setNewTypeCount(String newTypeCount) {
        this.newTypeCount = newTypeCount;
    }

    public String getDeviceTotal() {
        return deviceTotal;
    }

    public void setDeviceTotal(String deviceTotal) {
        this.deviceTotal = deviceTotal;
    }

    public String getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(String normalCount) {
        this.normalCount = normalCount;
    }

    public String getAbnormalCount() {
        return abnormalCount;
    }

    public void setAbnormalCount(String abnormalCount) {
        this.abnormalCount = abnormalCount;
    }

    public String getTypeCount() {
        return typeCount;
    }

    public void setTypeCount(String typeCount) {
        this.typeCount = typeCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
