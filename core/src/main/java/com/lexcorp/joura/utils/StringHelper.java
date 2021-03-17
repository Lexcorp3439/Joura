package com.lexcorp.joura.utils;

import java.util.Random;

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
}
