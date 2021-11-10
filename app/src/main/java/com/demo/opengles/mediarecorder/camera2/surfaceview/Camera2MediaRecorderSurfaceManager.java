package com.demo.opengles.mediarecorder.camera2.surfaceview;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class Camera2MediaRecorderSurfaceManager {

    private Camera2PreviewHelper previewHelper;
    private Camera2RecordHelper recordHelper;

    public Camera2MediaRecorderSurfaceManager(Activity activity, int cameraId, SurfaceView surfaceView) {
        previewHelper = new Camera2PreviewHelper(activity, cameraId, surfaceView);
        recordHelper = new Camera2RecordHelper(activity, cameraId, surfaceView);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                previewHelper.startPreview();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    public void startRecorder() {
        previewHelper.stop();
        previewHelper.release();

        recordHelper.startRecord();
    }

    public void stopRecorder() {
        recordHelper.stop();
        recordHelper.release();

        previewHelper.startPreview();
    }

}