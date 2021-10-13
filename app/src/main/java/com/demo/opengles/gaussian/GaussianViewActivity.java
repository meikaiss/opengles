package com.demo.opengles.gaussian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.view.GaussianConfig;
import com.demo.opengles.gaussian.view.GaussianGLSurfaceView;
import com.demo.opengles.gaussian.view.provider.DecorShotBitmapProvider;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.ToastUtil;

public class GaussianViewActivity extends BaseActivity {

    private FrameLayout frameLayout;
    private WindowManager windowManager;

    private GaussianGLSurfaceView gaussianGLSurfaceView = null;
    private FrameLayout content;

    private boolean hasShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaussian_view);

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        frameLayout = findViewById(R.id.container);

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPms()) {
                    ToastUtil.show("模糊的背景透明、且普通view位于模糊层之上，必须借助OverLay");
                    return;
                }

                if (hasShow) {
                    ToastUtil.show("请不要重复添加");
                    return;
                }

                Activity activity = GaussianViewActivity.this;

                gaussianGLSurfaceView = new GaussianGLSurfaceView(activity);
                GaussianConfig config = GaussianConfig.createDefaultConfig();
                config.bitmapProvider = new DecorShotBitmapProvider(activity, frameLayout);
                config.clipDrawable = AppCompatResources.getDrawable(activity, R.drawable.ic_svg_test_real);
                config.repeatCount = 1;
                gaussianGLSurfaceView.setConfig(config);
                gaussianGLSurfaceView.init();

                content = new FrameLayout(activity);
                content.addView(LayoutInflater.from(activity).inflate(R.layout.pop_gaussian_one_filter, null));
                content.findViewById(R.id.tv_place).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.show("test");
                    }
                });

                WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS & ~WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);

                mParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                if (Build.VERSION.SDK_INT >= 26) {
                    mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }
                mParams.x = 30;
                mParams.y = 500;
                mParams.width = 800;
                mParams.height = 1000;

                WindowManager.LayoutParams mParams2 = new WindowManager.LayoutParams();
                mParams2.copyFrom(mParams);

                gaussianGLSurfaceView.setOnGlDrawFinishListener(new GaussianGLSurfaceView.OnGlDrawFinishListener() {
                    @Override
                    public void onGlDrawFinish() {
                        content.post(new Runnable() {
                            @Override
                            public void run() {
                                content.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                windowManager.addView(gaussianGLSurfaceView, mParams);
                content.setVisibility(View.INVISIBLE);
                windowManager.addView(content, mParams2);


                hasShow = true;
            }
        });

        findViewById(R.id.btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    windowManager.removeViewImmediate(gaussianGLSurfaceView);
                    windowManager.removeViewImmediate(content);
                    hasShow = false;
                } catch (Exception e) {

                }
            }
        });
    }

    private boolean checkPms() {
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            //启动Activity让用户授权
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 10);
            return false;
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            windowManager.removeViewImmediate(gaussianGLSurfaceView);
            windowManager.removeViewImmediate(content);
            hasShow = false;
        } catch (Exception e) {

        }
    }
}
