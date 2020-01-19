package com.code.common.util;

/**
 * @Description
 * @Author liufei
 * @Date 2020/1/14 16:22
 */
public class StringUtils {
    public static boolean isNotBlank(String s) {
        if (s != null && s.trim().length() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isBlank(String s) {
        if (s == null || s.trim().length() == 0) {
            return true;
        }
        return false;
    }
}
