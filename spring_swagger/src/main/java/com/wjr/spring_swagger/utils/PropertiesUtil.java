package com.wjr.spring_swagger.utils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.utils
 * @ClassName: PropertiesUtil
 * @create 2022-02-13 16:39
 * @Description:
 */
public class PropertiesUtil {
    public static Properties load(String propertiesName) {
        Properties properties = new Properties();
        try {

            //加载指定配置文件,文件是从classes中获取的，所以需要从当前线程的类加载器中获取
            properties.load(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesName),
                    StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static void main(String[] args) {
        Properties properties = load("config.properties");
        properties.setProperty("initialSize", properties.getProperty("spring.datasource.initialSize"));
        System.out.println(properties);
    }
}
