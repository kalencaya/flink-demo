package cn.sliew.flink.dw.common;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public enum LogUtil {
    ;

    public static void trace(Logger logger, String message, Object... params) {
        if (logger.isTraceEnabled()) {
            log(logger, Level.TRACE, message, params);
        }
    }

    public static void debug(Logger logger, String message, Object... params) {
        if (logger.isDebugEnabled()) {
            log(logger, Level.DEBUG, message, params);
        }
    }

    public static void info(Logger logger, String message, Object... params) {
        if (logger.isInfoEnabled()) {
            log(logger, Level.INFO, message, params);
        }
    }

    public static void warn(Logger logger, String message, Object... params) {
        if (logger.isWarnEnabled()) {
            log(logger, Level.WARN, message, params);
        }
    }

    public static void error(Logger logger, String message, Object... params) {
        if (logger.isErrorEnabled()) {
            log(logger, Level.ERROR, message, params);
        }
    }

    private static void log(Logger logger, Level level, String message, Object... params) {
        switch (level) {
            case TRACE:
                logger.trace(message, params);
                break;
            case DEBUG:
                logger.debug(message, params);
                break;
            case INFO:
                logger.info(message, params);
                break;
            case WARN:
                logger.warn(message, params);
                break;
            case ERROR:
                logger.error(message, params);
                break;
            default:
        }
    }
}
