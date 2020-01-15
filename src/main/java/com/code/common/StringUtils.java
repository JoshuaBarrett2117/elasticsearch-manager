package com.code.common;

/**
 * @Description
 * @Author liufei
 * @Date 2020/1/14 16:22
 */
public class StringUtils {
    public static boolean isNoBlank(String s) {
        if (s != null && s.length() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }
}
