package com.demo.opengles.gaussian;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.VectorDrawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.pop.one.ImplGaussianPop;
import com.demo.opengles.gaussian.pop.two.ImplFullScreenGaussianPop;
import com.demo.opengles.gaussian.render.HVBlurRenderObject;
import com.demo.opengles.gaussian.render.OneTexFilterRenderObject;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ToastUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/08/29.
 */
public class GaussianComplexActivity extends AppCompatActivity {

    private Button btnShow;
    private FrameLayout layoutGlSurfaceView;
    private FrameLayout layoutFragment;
    private EmptyFragment emptyFragment;

    private GLSurfaceView glSurfaceView;
    private HVBlurRenderObject renderObjectH;
    private HVBlurRenderObject renderObjectV;
    private OneTexFilterRenderObject defaultRenderObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaussian_complex);

        btnShow = findViewById(R.id.btn_show);
        layoutGlSurfaceView = findViewById(R.id.layout_gl_surface_view);
        layoutFragment = findViewById(R.id.layout_fragment);

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutGlSurfaceView.getChildCount() > 0) {
                    ToastUtil.show("请先隐藏");
                    return;
                }
                addSurfaceView();
            }
        });

        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (glSurfaceView == null) {
                    return;
                }

                layoutGlSurfaceView.removeView(glSurfaceView);
                glSurfaceView = null;

                getSupportFragmentManager().beginTransaction().remove(emptyFragment).commitNow();
                emptyFragment = null;
            }
        });

        findViewById(R.id.btn_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup viewGroup = findViewById(R.id.root);
                viewGroup.setBackgroundResource(R.mipmap.texture_image_markpolo);
            }
        });

        findViewById(R.id.btn_pop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeConsumeUtil.clear();
                TimeConsumeUtil.start("Gaussian.show");
                new ImplGaussianPop(GaussianComplexActivity.this).show(v);
            }
        });

        findViewById(R.id.btn_pop2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeConsumeUtil.clear();
                TimeConsumeUtil.start("FllGaussian.show");
                new ImplFullScreenGaussianPop(GaussianComplexActivity.this).show(v);

                if (objectAnimator[0] != null && objectAnimator[0].isRunning()) {
                    objectAnimator[0].end();
                }
                ViewGroup viewGroup = findViewById(R.id.root);
                objectAnimator[0] = ObjectAnimator.ofFloat(viewGroup, "translationX", 0, 1000)
                        .setDuration(5000);
                objectAnimator[0].setRepeatMode(ValueAnimator.RESTART);
                objectAnimator[0].setRepeatCount(ValueAnimator.INFINITE);

                objectAnimator[0].start();
            }
        });
    }

    final ObjectAnimator[] objectAnimator = {null};
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (objectAnimator[0] != null && objectAnimator[0].isRunning()) {
            objectAnimator[0].end();
        }
    }

    private void addSurfaceView() {
        glSurfaceView = new GLSurfaceView(this);
        layoutGlSurfaceView.addView(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        //支持背景透明-start
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setZOrderOnTop(true);
        //支持背景透明-end

        renderObjectH = new HVBlurRenderObject(this);
        renderObjectH.setBlurOffset(5, 0);
        renderObjectH.isBindFbo = true;

        renderObjectV = new HVBlurRenderObject(this);
        renderObjectV.setBlurOffset(0, 5);
        renderObjectV.isBindFbo = true;

        defaultRenderObject = new OneTexFilterRenderObject(this,
                getResources().getDrawable(R.drawable.ic_svg_test_real));
        defaultRenderObject.isBindFbo = false;

        glSurfaceView.setRenderer(renderer);

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private OnDrawFinishListener onDrawFinishListener = new OnDrawFinishListener() {
        @Override
        public void onDrawFinish() {
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (emptyFragment == null) {
                        emptyFragment = new EmptyFragment();
                        getSupportFragmentManager().beginTransaction().add(
                                R.id.layout_fragment, emptyFragment, "emptyFragment").commitNow();
                    }
                }
            });
        }
    };

    private interface OnDrawFinishListener {
        void onDrawFinish();
    }

    private GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {
        private int textureId;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            renderObjectH.onCreate();
            renderObjectV.onCreate();
            defaultRenderObject.onCreate();

            textureId = createTexture();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            renderObjectH.onChange(width, height);
            renderObjectV.onChange(width, height);
            defaultRenderObject.onChange(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            renderObjectH.onDraw(textureId);
            renderObjectV.onDraw(renderObjectH.fboTextureId);
            defaultRenderObject.onDraw(renderObjectV.fboTextureId);

            //初始化时的一次绘制，有可能造成没有模糊效果，原因暂不明确
            renderObjectH.onDraw(textureId);
            renderObjectV.onDraw(renderObjectH.fboTextureId);
            defaultRenderObject.onDraw(renderObjectV.fboTextureId);

            if (onDrawFinishListener != null) {
                onDrawFinishListener.onDrawFinish();
            }
        }

        private int createTexture() {
            int[] texture = new int[1];
            Bitmap textureBmp = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.texture_image_markpolo);

            Bitmap bitmapCopy = textureBmp.copy(Bitmap.Config.ARGB_8888, true);

            Bitmap clipBmp = Bitmap.createBitmap(textureBmp.getWidth(), textureBmp.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(clipBmp);
            VectorDrawable vectorDrawable =
                    (VectorDrawable) getResources().getDrawable(R.drawable.ic_svg_test_mask);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);

            Canvas canvas2 = new Canvas(bitmapCopy);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas2.drawBitmap(clipBmp, 0, 0, paint);

            ////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////
            //在显卡的纹理硬件组上选择当前活跃的纹理单元为：第0号纹理单元，默认为0
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
            GLES20.glGenTextures(1, texture, 0);
            textureId = texture[0];
            //将生成的纹理与gpu关联为2d纹理类型，传入纹理id作为参数，每次bing之后，后续操作的纹理都是该纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);

            //给纹理传入图像数据，至此，此纹理相关设置已经结束。后续想使用或者操作这个纹理，只要再glBindTexture这个纹理的id即可
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapCopy, 0);

            //返回生成的纹理的句柄
            return texture[0];
        }
    };

}
