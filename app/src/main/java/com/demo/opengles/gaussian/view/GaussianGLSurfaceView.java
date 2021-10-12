package com.demo.opengles.gaussian.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;

import com.demo.opengles.gaussian.render.HVBlurRenderObject;
import com.demo.opengles.gaussian.render.OneTexFilterRenderObject;
import com.demo.opengles.util.OpenGLESUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GaussianGLSurfaceView extends GLSurfaceView {

    private HVBlurRenderObject renderObjectH;
    private HVBlurRenderObject renderObjectV;
    private OneTexFilterRenderObject oneTexFilterRenderObject;

    private GaussianConfig config;
    private Bitmap bgBitmap;

    public GaussianGLSurfaceView(Context context) {
        super(context);
    }

    public GaussianGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setConfig(GaussianConfig config) {
        this.config = config;
    }

    public void init() {
        //支持背景透明-start
//        setZOrderOnTop(true);
//        setZOrderMediaOverlay(true);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        //支持背景透明-end

        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(false);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLContextClientVersion(3);

        if (config.bitmapCreateMode == GaussianConfig.BitmapCreateMode.ViewInit) {
            bgBitmap = config.bitmapProvider.getOriginBitmap();
        }

        renderObjectH = new HVBlurRenderObject(getContext());
        renderObjectH.setBlurRadius(config.blurRadius);
        renderObjectH.setBlurOffset(config.blurOffsetW, 0);
        renderObjectH.isBindFbo = true;

        renderObjectV = new HVBlurRenderObject(getContext());
        renderObjectV.setBlurRadius(config.blurRadius);
        renderObjectV.setBlurOffset(0, config.blurOffsetH);
        renderObjectV.isBindFbo = true;

        oneTexFilterRenderObject = new OneTexFilterRenderObject(getContext(), config.clipDrawable);
        oneTexFilterRenderObject.isBindFbo = false;

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


        post(new Runnable() {
            @Override
            public void run() {
                requestRender();
            }
        });
    }

    private GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {

        private int textureId;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            renderObjectH.onCreate();
            renderObjectV.onCreate();
            oneTexFilterRenderObject.onCreate();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            renderObjectH.onChange(width, height);
            renderObjectV.onChange(width, height);
            oneTexFilterRenderObject.onChange(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (config.bitmapCreateMode == GaussianConfig.BitmapCreateMode.GlDraw) {
                bgBitmap = config.bitmapProvider.getOriginBitmap();
            }

            textureId = OpenGLESUtil.createBitmapTextureId(bgBitmap, GLES20.GL_TEXTURE0);

            renderObjectH.onDraw(textureId);
            renderObjectV.onDraw(renderObjectH.fboTextureId);
            oneTexFilterRenderObject.onDraw(renderObjectV.fboTextureId);
        }

    };
}
