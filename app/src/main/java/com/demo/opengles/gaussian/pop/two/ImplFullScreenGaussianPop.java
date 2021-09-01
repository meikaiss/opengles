package com.demo.opengles.gaussian.pop.two;

import android.app.Activity;
import android.graphics.drawable.VectorDrawable;
import android.view.View;
import android.view.ViewGroup;

import com.demo.opengles.R;

public class ImplFullScreenGaussianPop extends AbsFullScreenGaussianPop {

    private ViewGroup viewGroupRoot;

    protected int getContentLayoutId() {
        return R.layout.pop_gaussian_two_filter;
    }

    @Override
    protected VectorDrawable getVectorDrawable() {
        return (VectorDrawable) context.getResources().getDrawable(R.drawable.ic_bg_svg_tip_dialog);
    }

    @Override
    protected int getDrawableWidth() {
        return 800;
    }

    @Override
    protected int getDrawableHeight() {
        return 500;
    }

    public ImplFullScreenGaussianPop(Activity activity) {
        super(activity);

        viewGroupRoot = findViewById(R.id.content_root);
        viewGroupRoot.setVisibility(View.GONE);
    }

    @Override
    protected void onDrawFinish() {
        super.onDrawFinish();
        viewGroupRoot.post(new Runnable() {
            @Override
            public void run() {
                viewGroupRoot.setVisibility(View.VISIBLE);
            }
        });
    }

    protected int getWidth() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    protected int getHeight() {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
