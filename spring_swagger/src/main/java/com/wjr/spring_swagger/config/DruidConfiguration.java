package com.wjr.spring_swagger.config;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.config
 * @ClassName: DruidConfiguration
 * @create 2022-02-12 20:27
 */

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * druid监控界面设置
 * （1）登陆地址:http://127.0.0.1:8080/druid/index.html(端口号看自己设置的端口号)
 * （2）一旦配置黑名单,那么该ip访问是没有权限的登陆的
 * （3）如果想看SQL执行结果,那么上面数据源配置一定要添加dataSource.setFilters(filters),我之前就没有添加,所以其它都能正常使用,就是无法监控Sql语句。
 */
@Configuration
public class DruidConfiguration {

    @Bean
    public ServletRegistrationBean druidStatViewServle() {
        //注册服务
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(
                new StatViewServlet(), "/druid/*");
        // 白名单(为空表示,所有的都可以访问,多个IP的时候用逗号隔开)
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
        // IP黑名单 (存在共同时，deny优先于allow) （黑白名单就是如果是黑名单，那么该ip无法登陆该可视化界面）
        servletRegistrationBean.addInitParameter("deny", "127.0.0.2");
        // 设置登录的用户名和密码
        servletRegistrationBean.addInitParameter("loginUsername", "root");
        servletRegistrationBean.addInitParameter("loginPassword", "123456");
        // 是否能够重置数据.
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean druidStatFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(
                new WebStatFilter());
        // 添加过滤规则
        filterRegistrationBean.addUrlPatterns("/*");
        // 添加不需要忽略的格式信息
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        System.out.println("druid初始化成功!");
        return filterRegistrationBean;

    }
}

