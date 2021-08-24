package com.demo.opengles.util;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtil {
    public static void setW(View view, int width) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = width;
        view.setLayoutParams(lp);
    }

    public static void setH(View view, int height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = height;
        view.setLayoutParams(lp);
    }
}
