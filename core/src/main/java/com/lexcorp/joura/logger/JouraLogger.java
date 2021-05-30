package com.lexcorp.joura.logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JouraLogger {
    Logger logger;
    String prefix;
    private final static String standardPrefix = " - ";

    static {
        InputStream stream = JouraLogger.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JouraLogger get(Class<?> clazz) {
        return get(clazz, standardPrefix);
    }

    public static JouraLogger get(Class<?> clazz, String prefix) {
        Logger logger = Logger.getLogger(clazz.getName());
        ;
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

    public void debug(String message) {
        logger.fine(message);
    }

    public void debug(Marker marker, String message) {
        logger.fine(messageWithMarker(marker, message));
    }

    public void warn(String message) {
        logger.warning(message);
    }

    public void warn(Marker marker, String message) {
        logger.warning(messageWithMarker(marker, message));
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(Marker marker, String message) {
        logger.info(messageWithMarker(marker, message));
    }

    public void severe(String message) {
        logger.severe(message);
    }

    public void severe(Marker marker, String message) {
        logger.severe(messageWithMarker(marker, message));
    }

    private String messageWithMarker(Marker marker, String message) {
        return '[' + marker.getName() + ']' + prefix + message;
    }
}
