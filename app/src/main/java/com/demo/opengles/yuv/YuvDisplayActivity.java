package com.demo.opengles.yuv;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class YuvDisplayActivity extends BaseActivity {

    private SurfaceView surfaceView;
    private GLSurfaceView glSurfaceView;

    private Camera camera;
    private YuvRenderer yuvRenderer = new YuvRenderer(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuv);

        surfaceView = findViewById(R.id.surface_view);
        glSurfaceView = findViewById(R.id.gl_surface_view);

        initCamera();
        initGlSurfaceView();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_0:
                yuvRenderer.setOrientation(0);
                break;
            case R.id.btn_90:
                yuvRenderer.setOrientation(90);
                break;
            case R.id.btn_180:
                yuvRenderer.setOrientation(180);
                break;
            case R.id.btn_270:
                yuvRenderer.setOrientation(270);
                break;
            case R.id.btn_360:
                yuvRenderer.setOrientation(360);
                break;
        }
    }

    private void initCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        int format = camera.getParameters().getPreviewFormat();
        Log.e("yuv", "previewFormat = " + format);
        if (format == ImageFormat.NV21) {
            Log.e("yuv", "previewFormat = ImageFormat.NV21");
        }

        SurfaceHolder holder = surfaceView.getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {

                        /**
                         * data 的长度为 width * height * 1.5
                         * 前面 width * height 长度的字节数据为 Y 信息
                         * 后面 width * height * 1.5 长度的字节数据为 UV 信息
                         */

                        Log.e("yuv-nv21", "data[0] = " + data[0] + ", length=" + data.length);

                        Camera.Size previewSize = camera.getParameters().getPreviewSize();
                        int width = previewSize.width;
                        int height = previewSize.height;

                        yuvRenderer.prepareYuvFrame(data, width, height);
                        glSurfaceView.requestRender();
                    }
                });
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
            }
        });
    }

    private void initGlSurfaceView() {
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                yuvRenderer.onCreate();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                yuvRenderer.onChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                yuvRenderer.onDraw(0);
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

}
