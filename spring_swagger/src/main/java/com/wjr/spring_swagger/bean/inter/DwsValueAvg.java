package com.wjr.spring_swagger.bean.inter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwsValueAvg
 * @create 2022-02-21 22:32
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DwsValueAvg {


    /**
     * voltage
     */
    private Double voltage;

    /**
     * temperature
     */
    private Double temperature;

    /**
     * humidity
     */
    private Double humidity;

    /**
     * lighting
     */
    private Double lighting;

    /**
     * pm2_5
     */
    private Double pm25;

    /**
     * co_2
     */
    private Double co2;

    private String dt;
}
