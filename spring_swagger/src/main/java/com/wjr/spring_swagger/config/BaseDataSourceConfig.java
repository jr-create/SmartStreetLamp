package com.wjr.spring_swagger.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.wjr.spring_swagger.utils.PropertiesUtil;

import java.util.Properties;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.config
 * @ClassName: BaseDataSourceConfig
 * @create 2022-02-12 21:53
 * @Description:
 */
public class BaseDataSourceConfig {



    public static DruidDataSource currencyDataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        try{
            Properties properties = new Properties();
            Properties config = PropertiesUtil.load("config.properties");
            properties.setProperty("druid.initialSize", config.getProperty("spring.datasource.initialSize"));
            properties.setProperty("druid.minIdle", config.getProperty("spring.datasource.minIdle"));
            properties.setProperty("druid.maxActive", config.getProperty("spring.datasource.maxActive"));
            properties.setProperty("druid.maxWait", config.getProperty("spring.datasource.maxWait"));
            properties.setProperty("druid.timeBetweenEvictionRunsMillis",config.getProperty( "spring.datasource.timeBetweenEvictionRunsMillis"));
            properties.setProperty("druid.minEvictableIdleTimeMillis", config.getProperty("spring.datasource.minEvictableIdleTimeMillis"));
            properties.setProperty("druid.validationQuery", config.getProperty("spring.datasource.validationQuery"));
            properties.setProperty("druid.testWhileIdle",config.getProperty( "spring.datasource.testWhileIdle"));
            properties.setProperty("druid.testOnBorrow", config.getProperty("spring.datasource.testOnBorrow"));
            properties.setProperty("druid.testWhileIdle", config.getProperty("spring.datasource.testWhileIdle"));
            properties.setProperty("druid.testOnReturn",config.getProperty( "spring.datasource.testOnReturn"));
            properties.setProperty("druid.poolPreparedStatements", config.getProperty("spring.datasource.poolPreparedStatements"));
            properties.setProperty("druid.maxPoolPreparedStatementPerConnectionSize",config.getProperty( "spring.datasource.maxPoolPreparedStatementPerConnectionSize"));
            properties.setProperty("druid.filters", config.getProperty("spring.datasource.filters"));
            properties.setProperty("druid.connectionProperties", config.getProperty("spring.datasource.connectionProperties"));
            //具体配置
            dataSource.configFromPropety(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataSource;
    }

}
