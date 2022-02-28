package com.wjr.spring_swagger.bean.inter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * reason
     */
    private String reason;

    /**
     * device_id
     */
    private String deviceId;

    /**
     * type_id
     */
    private Integer typeId;

    /**
     * road_id
     */
    private Integer roadId;

    /**
     * co_2
     */
    private String co2;

    /**
     * timestamp
     */
    private Long timestamp;

    /**
     * latitude
     */
    private String latitude;

    /**
     * longitude
     */
    private String longitude;
    /**
     * lighting
     */
    private String lighting;

    /**
     * temperature
     */
    private String temperature;

    /**
     * info
     */
    private String info;


    /**
     * direct
     */
    private String direct;

    /**
     * humidity
     */
    private String humidity;


    /**
     * power
     */
    private String power;

    /**
     * pm2_5
     */
    private String pm25;

    /**
     * error_code
     */
    private Integer errorCode;

    public DeviceInfo() {}
}
