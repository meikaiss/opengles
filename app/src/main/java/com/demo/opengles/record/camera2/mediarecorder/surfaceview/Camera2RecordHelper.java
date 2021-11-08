package com.demo.opengles.record.camera2.mediarecorder.surfaceview;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.demo.opengles.util.FormatUtil;
import com.demo.opengles.util.IOUtil;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Camera2RecordHelper {
    private Context context;
    private int cameraId;
    private SurfaceView surfaceView;


    private CameraManager cameraManager;
    private Size mPreviewSize; // 预览大小
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;

    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;

    private MediaRecorder mediaRecorder;
    private boolean isStartRecord;

    public Camera2RecordHelper(Context context, int cameraId, SurfaceView surfaceView) {
        this.context = context;
        this.cameraId = cameraId;
        this.surfaceView = surfaceView;

        cameraHandlerThread = new HandlerThread("Camera2RecordThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void startRecord() {
        try {
            doStartRecord();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void doStartRecord() throws CameraAccessException {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId + "");

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
        mPreviewSize = sizes[0];

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ToastUtil.show("没有访问相机权限");
            return;
        }

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                configMediaRecorder();
                try {
                    createCameraSessionRecord();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }, cameraThreadHandler);
    }

    private void createCameraSessionRecord() throws CameraAccessException {
        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surfaceView.getHolder().getSurface());
        surfaceList.add(mediaRecorder.getSurface());

        cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                cameraCaptureSession = session;

                try {
                    //注意长安的板子不允许设置这两项配置，否则无法预览
                    // 设置连续自动对焦
                    //builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    // 设置自动曝光
                    //builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                    CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                    for (int i = 0; i < surfaceList.size(); i++) {
                        builder.addTarget(surfaceList.get(i));
                    }

                    session.setRepeatingRequest(builder.build(), null, cameraThreadHandler);

                    mediaRecorder.start();
                    isStartRecord = true;

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, cameraThreadHandler);
    }

    public void stop() {
        try {
            doStop();
            isStartRecord = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void doStop() throws CameraAccessException {
        mediaRecorder.stop();
        if (cameraCaptureSession != null) {
            cameraCaptureSession.stopRepeating();
        }
    }

    public void release() {
        IOUtil.close(cameraCaptureSession);
        IOUtil.close(cameraDevice);
    }

    private void configMediaRecorder() {
        String time = FormatUtil.get_yyyy_MM_DD_HH_mm_ss();
        File file = new File(context.getExternalCacheDir(), "camera_" + cameraId + "_" + time + ".mp4");

        if (file.exists()) {
            file.delete();
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频来源
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//设置视频来源
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//设置输出格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择AAC
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);//设置视频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择H264
        mediaRecorder.setVideoEncodingBitRate(1 * mPreviewSize.getWidth() * mPreviewSize.getHeight());//设置比特率 一般是 1*分辨率 到 10*分辨率 之间波动。比特率越大视频越清晰但是视频文件也越大。
        mediaRecorder.setVideoFrameRate(25);//设置帧数 选择 30即可， 过大帧数也会让视频文件更大当然也会更流畅，但是没有多少实际提升。人眼极限也就30帧了。
        mediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        mMediaRecorder.setOrientationHint(90);
//        mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}