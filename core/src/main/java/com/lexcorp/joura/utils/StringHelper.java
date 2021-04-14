package com.lexcorp.joura.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

public class StringHelper {

    public static String createFieldName() {
        return generateRandomString("var", 10, true, true);
    }

    public static String createMethodName() {
        return generateRandomString("method", 10, true, true);
    }

    public static String generateRandomString(int length, boolean useLetters, boolean useNumbers) {
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public static String generateRandomString(String base, int length, boolean useLetters, boolean useNumbers) {
        return base + generateRandomString(length, useLetters, useNumbers);
    }

    public static Map<String, Object> mapOf(Object... args) {
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i = i + 2) {
            map.put((String) args[i], args[i + 1]);
        }
        return map;
    }
}
