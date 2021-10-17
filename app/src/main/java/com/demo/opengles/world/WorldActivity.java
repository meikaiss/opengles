package com.demo.opengles.world;

import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

/**
 * Created by meikai on 2021/10/16.
 */
public class WorldActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;


    World world = new World();
    Cube cube = new Cube();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
            @Override
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                return null;
            }
        });


        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                Log.e("mmm", "scaleFactor = " + scaleFactor);
                world.eyeRadius /= scaleFactor;
                world.resetMatrixFlag = true;

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        });

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                scaleGestureDetector.onTouchEvent(event);

                return world.onTouch(event);
            }
        });


        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {


            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                cube.create();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                cube.change(gl, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                world.draw();
                cube.draw(world.getMVPMatrix());
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    private EGLConfig f() {
        int confAttr[] = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_SAMPLE_BUFFERS, 1,//fbo抗锯齿和使用glBindFramebuffer要去掉
                EGL14.EGL_SAMPLES, 4,//fbo抗锯齿使用glBindFramebuffer要去掉
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(eglDis, confAttr, 0, configs, 0, 1, numConfigs, 0);

        return configs[0];
    }

}
