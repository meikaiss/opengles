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
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.IOUtil;
import com.demo.opengles.util.OpenGLESUtil;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Camera2GLSurfaceViewPreviewManager {

    private Context context;
    private GLSurfaceView glSurfaceView;
    private int cameraId;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private CameraDevice camera;
    private CameraCaptureSession session;

    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;

    private int cameraTextureId;
    private SurfaceTexture cameraSurfaceTexture;
    private Surface surface;

    private CameraRenderObject cameraRenderObject;
    private DefaultRenderObject defaultRenderObject;

    public void create(Context context, GLSurfaceView glSurfaceView, int cameraId) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
        this.cameraId = cameraId;

        cameraRenderObject = new CameraRenderObject(context);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        defaultRenderObject = new DefaultRenderObject(context);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;

        cameraHandlerThread = new HandlerThread("cameraOpenThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                cameraTextureId = OpenGLESUtil.getOesTexture();
                cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);
                surface = new Surface(cameraSurfaceTexture);

                cameraSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        glSurfaceView.requestRender();
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

        glSurfaceView.setRenderMode(EglSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @SuppressLint("MissingPermission")
    private void openCamera2() throws Exception {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        characteristics = cameraManager.getCameraCharacteristics(cameraId + "");

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2GLSurfaceViewPreviewManager.this.camera = camera;

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
        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(surface);

        camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2GLSurfaceViewPreviewManager.this.session = session;

                try {
                    //注意长安的板子不允许设置这两项配置，否则无法预览
//                    // 设置连续自动对焦
//                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
//                            .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                    // 设置自动曝光
//                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
//                            .CONTROL_AE_MODE_ON_AUTO_FLASH);

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
        try {
            session.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        IOUtil.close(session);
        IOUtil.close(camera);
        cameraHandlerThread.quitSafely();
    }

}
