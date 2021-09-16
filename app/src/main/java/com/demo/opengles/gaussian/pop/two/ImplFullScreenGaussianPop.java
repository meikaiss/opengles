package com.demo.opengles.gaussian.pop.two;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.VectorDrawable;
import android.view.View;
import android.view.ViewGroup;

import com.demo.opengles.R;

public class ImplFullScreenGaussianPop extends AbsFullScreenGaussianPop {

    private ViewGroup viewGroupRoot;
    private ViewGroup viewGroupDelConfirm;


    protected int getContentLayoutId() {
        return R.layout.pop_gaussian_two_filter;
    }

    @Override
    protected VectorDrawable getVectorDrawable() {
        return (VectorDrawable) activity.getResources().getDrawable(R.drawable.ic_bg_svg_tip_dialog);
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

        viewGroupDelConfirm = findViewById(R.id.layout_del_content);
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
        return activity.getResources().getDisplayMetrics().widthPixels;
    }

    protected int getHeight() {
        return activity.getResources().getDisplayMetrics().heightPixels;
    }

}
