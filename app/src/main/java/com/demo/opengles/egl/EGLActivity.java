package com.demo.opengles.egl;

import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.sdk.EglSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;


public class EGLActivity extends AppCompatActivity {

    private static final String TAG = "EGLActivity";

    private EglSurfaceView eglSurfaceView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.egl_activity);

        eglSurfaceView = findViewById(R.id.egl_surface_view);
        eglSurfaceView.setRenderer(renderer);
        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_WHEN_DIRTY);

        eglSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                eglSurfaceView.requestRender();
                return true;
            }
        });

        printInfo();
    }

    private void printInfo() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        egl.eglInitialize(eglDisplay, version);
        Log.e(TAG, "version=" + version[0] + "." + version[1]);

        String vendor = egl.eglQueryString(eglDisplay, EGL10.EGL_VENDOR);
        Log.e(TAG, "EGL_VENDOR=" + vendor);

        String versionStr = egl.eglQueryString(eglDisplay, EGL10.EGL_VERSION);
        Log.e(TAG, "EGL_VERSION=" + versionStr);

        String extensionStr = egl.eglQueryString(eglDisplay, EGL10.EGL_EXTENSIONS);
        Log.e(TAG, "EGL_EXTENSIONS=" + extensionStr);
    }

    private EglSurfaceView.Renderer renderer = new EglSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated() {
            Log.e(TAG, "onSurfaceCreated");
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            Log.e(TAG, "onSurfaceChanged");
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame() {
            Log.e(TAG, "onDrawFrame");
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            float r = (float) Math.random();
            float g = (float) Math.random();
            float b = (float) Math.random();

            GLES20.glClearColor(r, g, b, 1.0f);
        }
    };
}
