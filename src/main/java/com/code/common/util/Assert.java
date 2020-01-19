package com.code.common.util;

public class Assert {

    public static void isTrue(boolean b) {
        if (!b) {
            throw new IllegalArgumentException("表达式为false");
        }
    }

    public static void isTrue(boolean b, String msg) {
        if (!b) {
            throw new IllegalArgumentException(msg);
        }
    }
}
