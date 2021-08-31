package com.demo.opengles.gaussian;

import android.app.Activity;
import android.graphics.drawable.VectorDrawable;
import android.view.View;
import android.widget.TextView;

import com.demo.opengles.R;

public class ImplGaussianPop extends AbsGaussianPop {

    private TextView tvPlace;
    private TextView tvTime;

    public ImplGaussianPop(Activity activity) {
        super(activity);
        tvPlace = findViewById(R.id.tv_place);
        tvTime = findViewById(R.id.tv_time);

        tvPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindow.dismiss();
            }
        });
    }

    protected int getContentLayoutId() {
        return R.layout.pop_window_gaussian_place_time;
    }

    @Override
    protected VectorDrawable getVectorDrawable() {
        return (VectorDrawable) context.getResources().getDrawable(R.drawable.ic_svg_test_mask);
    }

    protected int getWidth() {
        return 228 * 3;
    }

    protected int getHeight() {
        return 319 * 3;
    }


}
