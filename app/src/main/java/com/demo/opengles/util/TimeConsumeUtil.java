package com.demo.opengles.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeConsumeUtil {
    private static final String TAG = "TimeConsumeUtil";

    private static Map<String, List<Long>> timeConsumeMap = new HashMap<>();

    public static void clear() {
        timeConsumeMap.clear();
    }

    public static void start(String flag) {
        List<Long> value = new ArrayList<>();
        value.add(System.currentTimeMillis());
        timeConsumeMap.put(flag, value);
    }

    public static void calc(String flag) {
        calc(flag, null);
    }

    public static void end(String flag) {
        calc(flag, null);
        timeConsumeMap.remove(flag);
    }

    public static void direct(String flag) {
        Log.e(TAG, "耗时统计, flag=" + flag + ", " + System.currentTimeMillis());
    }

    public static void calc(String flag, String alias) {
        List<Long> value = timeConsumeMap.get(flag);
        if (value == null || value.isEmpty()) {
            Log.e(TAG, "耗时统计, flag=" + flag + ", 此事件未开始");
            return;
        }

        value.add(System.currentTimeMillis());
        Long l1 = value.get(value.size() - 1);
        Long l2 = value.get(value.size() - 2);
        if (l1 == null || l2 == null) {
            Log.e(TAG, "耗时统计, flag=" + flag + ", 调用时机不对，必须先调用start, " + Thread.currentThread().getName() + ", "
                    + System.currentTimeMillis());
            return;
        }

        long consume = l1 - l2;

        if (alias == null) {
            Log.e(TAG, "耗时统计, flag=" + flag + ", " + consume + "ms,  " + Thread.currentThread().getName() + ", "
                    + System.currentTimeMillis());
        } else {
            Log.e(TAG, "耗时统计, flag=" + flag + ", " + alias + ", " + consume + "ms,  " + Thread.currentThread().getName()
                    + ", " + System.currentTimeMillis());
        }
    }

}
