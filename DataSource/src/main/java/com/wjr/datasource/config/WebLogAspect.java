package com.wjr.datasource.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 全局日志
 */
@Component
@Aspect
public class WebLogAspect {
    public static final Logger logger = LoggerFactory.getLogger("");

    @Pointcut("execution(public * com.wjr.datasource.controller..*.*(..))")
    public void webLog(){}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        logger.info("URL:" + request.getRequestURL().toString());
        logger.info("HTTP_METHOD:" + request.getMethod());
        logger.info("IP:" + request.getRemoteAddr());
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String s = parameterNames.nextElement();
            logger.info("name:{},value:{}", s, request.getParameter(s));
        }
    }

    @AfterReturning(returning = "obj",pointcut = "webLog()")
    public void doAfterReturning(Object obj) throws Throwable{
        //处理完请求，返回内容
        logger.info("RESPONSE:"+obj);
    }
}
