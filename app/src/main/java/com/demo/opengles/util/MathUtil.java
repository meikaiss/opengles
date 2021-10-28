package com.demo.opengles.util;

public class MathUtil {

    public static int clamp(int target, int min, int max) {
        target = Math.max(min, target);
        target = Math.min(max, target);
        return target;
    }

}
