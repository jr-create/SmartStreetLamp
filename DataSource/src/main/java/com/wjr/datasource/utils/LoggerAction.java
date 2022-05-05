package com.wjr.datasource.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 29375-wjr
 * @Package: com.wjr.datasource.utils
 * @ClassName: Logger
 * @create 2022-05-05 10:39
 * @Description:
 */
public class LoggerAction {
    //定义一个全局的记录器，通过LoggerFactory获取
    public static final  Logger logger = LoggerFactory.getLogger("");
    public LoggerAction(){}

    public static void main(String[] args) {
        logger.info("sdfsd");
        logger.debug("sdfsd");
        logger.error("sdfsd");
    }
}
