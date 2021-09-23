package com.demo.opengles.record.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.OpenGLESUtil;

import java.util.ArrayList;
import java.util.List;

public class Camera2RecordManager {

    private Context context;
    private SurfaceView eglSurfaceView;
    private int cameraId;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private CameraDevice camera;
    private CameraCaptureSession session;

    private int cameraTextureId;
    private SurfaceTexture cameraSurfaceTexture;
    private Surface surface;

    private CameraRenderObject cameraRenderObject;
    private DefaultRenderObject defaultRenderObject;

    public void create(Context context, SurfaceView eglSurfaceView, int cameraId) {
        this.context = context;
        this.eglSurfaceView = eglSurfaceView;
        this.cameraId = cameraId;

        cameraRenderObject = new CameraRenderObject(context);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        defaultRenderObject = new DefaultRenderObject(context);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;


        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId + "");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        eglSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Camera2RecordManager.this.surface = holder.getSurface();
                try {
                    initCamera2(surface);
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

//        eglSurfaceView.setRenderer(new EglSurfaceView.Renderer() {
//            @Override
//            public void onSurfaceCreated() {
//                cameraTextureId = OpenGLESUtil.getOesTexture();
//                cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);
//                surface = new Surface(cameraSurfaceTexture);
//
//                cameraRenderObject.onCreate();
//                defaultRenderObject.onCreate();
//
//                try {
//                    initCamera2(surface);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onSurfaceChanged(int width, int height) {
//                cameraRenderObject.onChange(width, height);
//                defaultRenderObject.onChange(width, height);
//            }
//
//            @Override
//            public void onDrawFrame() {
//                cameraSurfaceTexture.updateTexImage();
//                cameraRenderObject.onDraw(cameraTextureId);
//                defaultRenderObject.onDraw(cameraRenderObject.fboTextureId);
//            }
//        });

//        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @SuppressLint("MissingPermission")
    private void initCamera2(Surface surface) throws Exception {
        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2RecordManager.this.camera = camera;

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
        }, null);

    }

    private void createCameraSession() throws CameraAccessException {
        List<Surface> targets = new ArrayList<>();
        targets.add(surface);

        camera.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2RecordManager.this.session = session;

                try {
                    startRequest();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, null);
    }

    private void startRequest() throws CameraAccessException {
        CaptureRequest.Builder builder =
                camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(eglSurfaceView.getHolder().getSurface());

        session.setRepeatingRequest(builder.build(), null, null);
    }

    public void onDestroy() {

    }
}
