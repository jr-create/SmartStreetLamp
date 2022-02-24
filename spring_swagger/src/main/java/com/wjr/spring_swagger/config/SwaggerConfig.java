package com.wjr.spring_swagger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;

@Configuration
public class SwaggerConfig {

    /**
     * 分组配置
     *
     * @param environment
     * @return
     */
    @Bean
    public Docket docket(Environment environment) {
        // TODO: 2022/1/29 显示指定的环境
        Profiles devFiles = Profiles.of("dev");

        // TODO: 2022/1/29 判断是否处在自己设定的环境中
        boolean acceptsProfiles = environment.acceptsProfiles(devFiles);

        return new Docket(DocumentationType.SWAGGER_2).groupName("wjr")
                .enable(!acceptsProfiles)// TODO: 2022/1/29 是否启动swagger
                // TODO: 2022/1/29 配置swagger前缀信息
                .apiInfo(apiInfo())
                .select()
                /**
                 * RequestHandlerSelectors:配置扫描接口的方式
                 * basePackage：指定扫描的包
                 * any；扫描全部
                 * none：不扫描
                 * withClassAnnotation(RestController.class):扫描类上的指定注解，参数是一个反射对象
                 * withMethodAnnotation(GetMapping.class):扫描方法上的指定注解
                 */
                .apis(RequestHandlerSelectors.basePackage("com.wjr.spring_swagger.controller"))
//                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
//                .apis(RequestHandlerSelectors.withMethodAnnotation(GetMapping.class))
                // TODO: 2022/1/29 paths():指定扫描的路径
//                .paths(PathSelectors.ant("/index/*"))
                .build();
    }

    /**
     * 组2，配置
     */
    @Bean
    public Docket docket2(Environment environment) {

        return new Docket(DocumentationType.SWAGGER_2).groupName("wjr2")
                .enable(true)// TODO: 2022/1/29 是否启动swagger
                // TODO: 2022/1/29 配置swagger前缀信息
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .build();
    }

    private ApiInfo apiInfo() {
        // TODO: 2022/1/29 作者信息
        Contact contact = new Contact("wjr", "url", "email");
        return new ApiInfo(
                "title：API文档",
                "desc:描述信息",
                "v1.0版本",
                "http://localhost:8080",
                contact,
                "Apache 2.0",
                "http://localhost:8080",
                new ArrayList<>()
        );
    }

}
