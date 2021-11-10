package com.demo.opengles.mediarecorder.camera2.surfaceview;

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
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.demo.opengles.util.IOUtil;
import com.demo.opengles.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class Camera2PreviewHelper {
    private Context context;
    private int cameraId;
    private SurfaceView surfaceView;


    private CameraManager cameraManager;
    private Size mPreviewSize; // 预览大小
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;

    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;

    private PreviewState previewState;

    public void setPreviewState(PreviewState previewState) {
        this.previewState = previewState;
    }

    public enum PreviewState {
        Idle,
        Preview,
        PreviewFail,
        Stop,
        StopFail,
        Release
    }

    public Camera2PreviewHelper(Context context, int cameraId, SurfaceView surfaceView) {
        this.context = context;
        this.cameraId = cameraId;
        this.surfaceView = surfaceView;

        cameraHandlerThread = new HandlerThread("Camera2PreviewThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        setPreviewState(PreviewState.Idle);
    }

    public void startPreview() {
        try {
            doStartPreview();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            setPreviewState(PreviewState.PreviewFail);
        }
    }

    private void doStartPreview() throws CameraAccessException {
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

                try {
                    createCameraSessionPreview();
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

                    CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    for (int i = 0; i < surfaceList.size(); i++) {
                        builder.addTarget(surfaceList.get(i));
                    }

                    session.setRepeatingRequest(builder.build(), null, cameraThreadHandler);
                    setPreviewState(PreviewState.Preview);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    setPreviewState(PreviewState.PreviewFail);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                setPreviewState(PreviewState.PreviewFail);
            }
        }, cameraThreadHandler);
    }

    public void stop() {
        try {
            doStop();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            setPreviewState(PreviewState.StopFail);
        }
    }

    private void doStop() throws CameraAccessException {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.stopRepeating();
            setPreviewState(PreviewState.Stop);
        }
    }

    public void release() {
        IOUtil.close(cameraCaptureSession);
        IOUtil.close(cameraDevice);
        setPreviewState(PreviewState.Release);
    }

}
