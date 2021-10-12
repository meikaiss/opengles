package com.demo.opengles.gaussian.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;

import com.demo.opengles.util.ViewUtil;

public class DecorShotBitmapProvider implements IBitmapProvider {

    private Activity activity;
    private View targetView;

    public DecorShotBitmapProvider(Activity activity, View targetView) {
        this.activity = activity;
        this.targetView = targetView;
    }

    @Override
    public Bitmap getOriginBitmap() {
        Bitmap bitmap = screenShot();
        Bitmap clipBmp = clip(bitmap);
        return clipBmp;
    }

    private Bitmap screenShot() {
        View view = activity.getWindow().getDecorView();

        view.buildDrawingCache();

        //状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int stateBarHeight = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        //获取屏幕宽高
        int widths = display.getWidth();
        int height = display.getHeight();

        //设置允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        //去掉状态栏高度、底部导航栏的高度
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, stateBarHeight, widths, height);

        view.destroyDrawingCache();
        return bitmap;
    }

    private Bitmap clip(Bitmap bitmap) {
        int[] location = new int[2];
        targetView.getLocationOnScreen(location);  //此坐标为view在屏幕中的坐标，包含了状态栏，而ACtivity截图是无法截状态栏
        location[1] -= ViewUtil.getStatusBarHeight(activity);

        Bitmap clipBmp = Bitmap.createBitmap(bitmap, location[0], location[1],
                targetView.getWidth(), targetView.getHeight());

        return clipBmp;
    }
}
