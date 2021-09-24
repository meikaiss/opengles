package com.demo.opengles.record.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.OpenGLESUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Camera2GLSurfaceViewRecordManager {

    private Context context;
    private GLSurfaceView eglSurfaceView;
    private int cameraId;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private CameraDevice camera;
    private CameraCaptureSession session;

    private int cameraTextureId;
    private SurfaceTexture cameraSurfaceTexture;
    private Surface surface;
    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;
    private Size mPreviewSize;

    private CameraRenderObject cameraRenderObject;
    private DefaultRenderObject defaultRenderObject;

    public void create(Context context, GLSurfaceView eglSurfaceView, int cameraId) {
        this.context = context;
        this.eglSurfaceView = eglSurfaceView;
        this.cameraId = cameraId;

        cameraRenderObject = new CameraRenderObject(context);
        cameraRenderObject.isBindFbo = false;
        cameraRenderObject.isOES = true;
        defaultRenderObject = new DefaultRenderObject(context);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;

        cameraHandlerThread = new HandlerThread("cameraOpenThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        eglSurfaceView.setEGLContextClientVersion(2);
        eglSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                try {
                    characteristics = cameraManager.getCameraCharacteristics(cameraId + "");
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics
                            .SCALER_STREAM_CONFIGURATION_MAP);

                    Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());

                    mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                            eglSurfaceView.getWidth(), eglSurfaceView.getHeight(), largest);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }


                cameraTextureId = OpenGLESUtil.getOesTexture();
                cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);
                cameraSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                surface = new Surface(cameraSurfaceTexture);

                cameraSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        eglSurfaceView.requestRender();
                    }
                });

                cameraRenderObject.onCreate();
                defaultRenderObject.onCreate();

                try {
                    openCamera2();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                cameraRenderObject.onChange(width, height);
                defaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                cameraSurfaceTexture.updateTexImage();
                cameraRenderObject.onDraw(cameraTextureId);
                defaultRenderObject.onDraw(cameraRenderObject.fboTextureId);
            }
        });

        eglSurfaceView.setRenderMode(EglSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @SuppressLint("MissingPermission")
    private void openCamera2() throws Exception {
        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2GLSurfaceViewRecordManager.this.camera = camera;

                try {
                    createCameraSession();
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

    private void createCameraSession() throws CameraAccessException {
        CaptureRequest.Builder builder =
                camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(surface);

        camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2GLSurfaceViewRecordManager.this.session = session;

                try {
                    // 设置连续自动对焦
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
                            .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    // 设置自动曝光
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
                            .CONTROL_AE_MODE_ON_AUTO_FLASH);

                    session.setRepeatingRequest(builder.build(), null, cameraThreadHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, cameraThreadHandler);
    }

    public void onDestroy() {
        session.close();
        camera.close();
        cameraHandlerThread.quitSafely();
    }

    private Size chooseOptimalSize(Size[] sizes, int viewWidth, int viewHeight, Size pictureSize) {
        int totalRotation = 0;
        boolean swapRotation = totalRotation == 90 || totalRotation == 270;
        int width = swapRotation ? viewHeight : viewWidth;
        int height = swapRotation ? viewWidth : viewHeight;
        return getSuitableSize(sizes, width, height, pictureSize);
    }

    private Size getSuitableSize(Size[] sizes, int width, int height, Size pictureSize) {
        int minDelta = Integer.MAX_VALUE; // 最小的差值，初始值应该设置大点保证之后的计算中会被重置
        int index = 0; // 最小的差值对应的索引坐标
        float aspectRatio = pictureSize.getHeight() * 1.0f / pictureSize.getWidth();
        for (int i = 0; i < sizes.length; i++) {
            Size size = sizes[i];
            // 先判断比例是否相等
            if (size.getWidth() * aspectRatio == size.getHeight()) {
                int delta = Math.abs(width - size.getWidth());
                if (delta == 0) {
                    return size;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return sizes[index];
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
