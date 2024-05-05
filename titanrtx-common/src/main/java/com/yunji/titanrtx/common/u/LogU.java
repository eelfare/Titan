package com.yunji.titanrtx.common.u;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdditionalLog
 *
 * @author leihz
 * @since 2020-06-04 3:01 下午
 */

public class LogU {
    private static final Logger log = LoggerFactory.getLogger(LogU.class.getName());

    public static void info(String format, Object... arguments) {
        log.info(format, arguments);
    }

    public static void warn(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    public static void error(String format, Object... arguments) {
        log.error(format, arguments);
    }
}
