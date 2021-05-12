package com.lexcorp.joura.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

public class JouraLogger {
    Logger logger;
    String prefix;
    private final static String standardPrefix = " - ";

    public static JouraLogger get(Class<?> clazz) {
        return get(clazz, standardPrefix);
    }

    public static JouraLogger get(Class<?> clazz, String prefix) {
        Logger logger = LogManager.getLogger(clazz);
        return new JouraLogger(logger, prefix);
    }

    public JouraLogger(Logger logger) {
        this.logger = logger;
    }

    public JouraLogger(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
    }

    public JouraLogger setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(Marker marker, String message) {
        logger.info(marker, messageWithMarker(marker, message));
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(Marker marker, String message) {
        logger.debug(marker, messageWithMarker(marker, message));
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(Marker marker, String message) {
        logger.error(marker, messageWithMarker(marker, message));
    }

    private String messageWithMarker(Marker marker, String message) {
        return '[' + marker.getName() + ']' + prefix + message;
    }
}
