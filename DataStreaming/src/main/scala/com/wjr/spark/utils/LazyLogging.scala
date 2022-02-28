package com.wjr.spark.utils

import org.slf4j.{Logger, LoggerFactory}

trait LazyLogging {

    protected lazy val logger: Logger =
        LoggerFactory.getLogger("smartLamp")

    // protected lazy val httpLogger: Logger =
    //    LoggerFactory.getLogger("http")

}
