package com.demo.opengles.leak;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.OpenGLESUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LeakGlSurfaceViewActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;

    private DefaultRenderObject defaultRenderObject;
    private int textureId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leak_glsurfaceview);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            Bitmap textureBmp;
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                defaultRenderObject = new DefaultRenderObject(LeakGlSurfaceViewActivity.this);
                defaultRenderObject.onCreate();

                textureBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image_markpolo);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                defaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                float r = (float) Math.random() * 100f / 100;
                float g = (float) Math.random() * 100f / 100;
                float b = (float) Math.random() * 100f / 100;
                GLES20.glClearColor(r, g, b, 1);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                OpenGLESUtil.deleteTextureId(textureId);

                textureId = OpenGLESUtil.createBitmapTextureId(textureBmp, GLES20.GL_TEXTURE0);

                /**
                 * Bitmap????????????java??????native???????????????????????????native????????????Bitmap????????????????????????recycle()?????????????????????????????????
                 * ??????????????????GC???????????????????????????????????????????????????
                 */
//                textureBmp.recycle();

                defaultRenderObject.onDraw(textureId);
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
