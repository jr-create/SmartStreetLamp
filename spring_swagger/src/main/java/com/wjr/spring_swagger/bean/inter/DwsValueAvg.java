package com.wjr.spring_swagger.bean.inter;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean
 * @ClassName: DwsValueAvg
 * @create 2022-02-21 22:32
 * @Description:
 */
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


    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getLighting() {
        return lighting;
    }

    public void setLighting(Double lighting) {
        this.lighting = lighting;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getCo2() {
        return co2;
    }

    public void setCo2(Double co2) {
        this.co2 = co2;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }
}
