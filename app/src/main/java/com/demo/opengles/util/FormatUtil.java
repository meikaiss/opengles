package com.demo.opengles.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {

    public static final String DATA_TIME = "yyyy_MM_dd_HH_mm_ss";

    public static String getFormatTime(String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr, Locale.getDefault());
        return format.format(new Date());
    }

    public static String get_yyyy_MM_DD_HH_mm_ss() {
        return getFormatTime(DATA_TIME);
    }
}
