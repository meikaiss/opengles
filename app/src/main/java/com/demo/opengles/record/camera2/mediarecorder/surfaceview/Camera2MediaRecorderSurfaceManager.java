package com.demo.opengles.record.camera2.mediarecorder.surfaceview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera2MediaRecorderSurfaceManager {
    private static final String TAG = "MRSM";

    private Activity activity;
    private int cameraId;
    private SurfaceView surfaceView;

    private MediaRecorder mMediaRecorder;
    private boolean isRecorder = false;//用于判断当前是否在录制视频

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private Size mPreviewSize; // 预览大小
    private CameraDevice camera;
    private CameraCaptureSession session;
    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;
    private Surface surface;

    public Camera2MediaRecorderSurfaceManager(Activity activity, int cameraId, SurfaceView surfaceView) {
        this.activity = activity;
        this.cameraId = cameraId;
        this.surfaceView = surfaceView;

        cameraHandlerThread = new HandlerThread("CameraThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    openCamera2(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void openCamera2(boolean isPreview) throws Exception {
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        characteristics = cameraManager.getCameraCharacteristics(cameraId + "");
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        //硬件层特征，为了让图像在手机屏幕直立显示时，需要将图像顺时针旋转此角度。不同手机厂商的此角度值会有不同
        //framework-sdk特性：这里获取到的size的宽高的方向是针对屏幕竖屏的上方向的宽高(此宽高与orientation无关)
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
        mPreviewSize = sizes[0];

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2MediaRecorderSurfaceManager.this.camera = camera;

                try {
                    if (isPreview) {
                        createCameraSessionPreview();
                    } else {
                        createCameraSessionRecord();
                    }

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

    private void createCameraSessionPreview() throws CameraAccessException {
        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surfaceView.getHolder().getSurface());
//        surfaceList.add(mMediaRecorder.getSurface());

        camera.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2MediaRecorderSurfaceManager.this.session = session;

                try {
                    //注意长安的板子不允许设置这两项配置，否则无法预览
//                    // 设置连续自动对焦
//                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                    // 设置自动曝光
//                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                    CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    for (int i = 0; i < surfaceList.size(); i++) {
                        builder.addTarget(surfaceList.get(i));
                    }

                    session.setRepeatingRequest(builder.build(), null, cameraThreadHandler);
//                    mMediaRecorder.start();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, cameraThreadHandler);
    }

    private void createCameraSessionRecord() throws CameraAccessException {
        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surfaceView.getHolder().getSurface());
        surfaceList.add(mMediaRecorder.getSurface());

        camera.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2MediaRecorderSurfaceManager.this.session = session;

                try {
                    //注意长安的板子不允许设置这两项配置，否则无法预览
//                    // 设置连续自动对焦
//                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                    // 设置自动曝光
//                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                    CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    for (int i = 0; i < surfaceList.size(); i++) {
                        builder.addTarget(surfaceList.get(i));
                    }

                    session.setRepeatingRequest(builder.build(), null, cameraThreadHandler);
                    mMediaRecorder.start();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, cameraThreadHandler);
    }

    /**
     * 配置MediaRecorder
     */
    private void configMediaRecorder() {
        File file = new File(activity.getExternalCacheDir(), "demo.mp4");
        if (file.exists()) {
            file.delete();
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频来源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//设置视频来源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//设置输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择AAC
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);//设置视频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择H264
        mMediaRecorder.setVideoEncodingBitRate(8 * 1024 * 1920);//设置比特率 一般是 1*分辨率 到 10*分辨率 之间波动。比特率越大视频越清晰但是视频文件也越大。
        mMediaRecorder.setVideoFrameRate(30);//设置帧数 选择 30即可， 过大帧数也会让视频文件更大当然也会更流畅，但是没有多少实际提升。人眼极限也就30帧了。
        mMediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启录制视频
     */
    public void startRecorder() {
        try {
            if (!isRecorder) {//如果不在录制视频
//                session.stopRepeating();
                session.close();
                session = null;

                configMediaRecorder();
                openCamera2(false);

                isRecorder = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "startRecorder, cameraId=" + cameraId + e.getMessage());
        }
    }

    /**
     * 停止录制视频
     */
    public void stopRecorder() {
        if (isRecorder) { //如果在录制视频
            try {
                mMediaRecorder.stop();//暂停录制
                mMediaRecorder.reset();//重置,将MediaRecorder调整为空闲状态
                isRecorder = false;

                mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());//设置预览
            } catch (Exception e) {
                Log.e(TAG, "stopRecorder, cameraId=" + cameraId + e.getMessage());
            }
        }
    }

    public void onDestroy() {
        try {
            if (mMediaRecorder != null) {
                if (isRecorder) {
                    mMediaRecorder.stop();
                }
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            if (camera != null) {
                camera.close();
                camera = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy, cameraId=" + cameraId + e.getMessage());
        }
    }

}