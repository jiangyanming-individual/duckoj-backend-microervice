package com.jiang.duckojbackendcommon.utils;

import java.util.regex.Pattern;

/**
 * 校验邮箱
 */

public class ValidEmailUtils {


    public static final String REGEX_EMAIL = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
    private static final Pattern pattern = Pattern.compile(REGEX_EMAIL);

    public static boolean validate(String email) {
        return pattern.matcher(email).matches();

    }
}
