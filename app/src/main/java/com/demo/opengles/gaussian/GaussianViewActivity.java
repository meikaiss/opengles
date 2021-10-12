package com.demo.opengles.gaussian;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.view.DecorShotBitmapProvider;
import com.demo.opengles.gaussian.view.GaussianConfig;
import com.demo.opengles.gaussian.view.GaussianGLSurfaceView;
import com.demo.opengles.gaussian.view.MegaBlurGLSurfaceView;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.ToastUtil;

public class GaussianViewActivity extends BaseActivity {

    private FrameLayout frameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaussian_view);

        frameLayout = findViewById(R.id.container);

        final int[] count = {0};

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frameLayout.getChildCount() > 0) {
                    ToastUtil.show("请先移除");
                    return;
                }
                Activity activity = GaussianViewActivity.this;

                View gaView = null;

                MegaBlurGLSurfaceView gaussianGLSurfaceView2 = new MegaBlurGLSurfaceView(activity);

                GaussianGLSurfaceView gaussianGLSurfaceView = new GaussianGLSurfaceView(activity);
                GaussianConfig config = GaussianConfig.createDefaultConfig();
                config.bitmapProvider = new DecorShotBitmapProvider(activity, frameLayout);
                config.bitmapCreateMode = GaussianConfig.BitmapCreateMode.ViewInit;
                config.clipDrawable = AppCompatResources.getDrawable(activity, R.drawable.ic_svg_test_real);
                gaussianGLSurfaceView.setConfig(config);
                gaussianGLSurfaceView.init();

                FrameLayout content = new FrameLayout(activity);
                content.addView(LayoutInflater.from(activity).inflate(R.layout.pop_gaussian_one_filter, null));

                if (count[0]++ % 2 == 0) {
                    gaView = gaussianGLSurfaceView;
                } else {
                    gaView = gaussianGLSurfaceView2;
                }
                frameLayout.addView(gaView);
                frameLayout.addView(content);
                frameLayout.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameLayout.removeAllViews();
            }
        });
    }


}
