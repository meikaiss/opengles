package com.demo.opengles.record.camera2.glrecordconcat;

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
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.record.camera1.AudioRecorder;
import com.demo.opengles.record.camera1.VideoRecordEncoder;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.FpsUtil;
import com.demo.opengles.util.IOUtil;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;


public class Camera2GLSurfaceViewConcatRecordManager {

    private Activity activity;
    private ViewGroup parent;
    private GLSurfaceView glSurfaceView;
    private int[] cameraIdArr;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private Size mPreviewSize; // 预览大小
    private CameraDevice[] camera = new CameraDevice[4];
    private CameraCaptureSession session;
    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;
    public boolean cameraOpenFlag[] = new boolean[4];

    private CameraNode[] cameraNodeArr = new CameraNode[4];
    private DefaultRenderObject offScreenRenderObject;
    private DefaultRenderObject screenRenderObject;

    private String savePath;
    private VideoRecordEncoder videoEncodeRecode;
    private AudioRecorder audioRecorder;

    public String getSavePath() {
        return savePath;
    }

    public void create(Activity activity, GLSurfaceView glSurfaceView, int[] cameraIdArr) {
        this.activity = activity;
        this.glSurfaceView = glSurfaceView;
        this.cameraIdArr = cameraIdArr;
        this.parent = (ViewGroup) glSurfaceView.getParent();
        parent.removeView(glSurfaceView);

        cameraHandlerThread = new HandlerThread("cameraOpenThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            openCamera2(this.cameraIdArr[0]);
            openCamera2(this.cameraIdArr[1]);
            openCamera2(this.cameraIdArr[2]);
            openCamera2(this.cameraIdArr[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera2(int cameraId) throws Exception {
        characteristics = cameraManager.getCameraCharacteristics(cameraId + "");
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
        mPreviewSize = sizes[0]; //直接选择最大的预览尺寸

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2GLSurfaceViewConcatRecordManager.this.camera[cameraId] = camera;

                cameraOpenFlag[cameraId] = true;
                triggerInitGlSurfaceView();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }, cameraThreadHandler);
    }

    private void triggerInitGlSurfaceView() {
        boolean allOpen = true;
        for (int i = 0; i < cameraOpenFlag.length; i++) {
            allOpen &= cameraOpenFlag[i];
        }

        if (allOpen) {
            initGlSurfaceView();
        }
    }

    private void initGlSurfaceView() {
        parent.addView(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);

        cameraNodeArr[0] = new CameraNode();
        cameraNodeArr[0].init(activity);
        cameraNodeArr[1] = new CameraNode();
        cameraNodeArr[1].init(activity);
        cameraNodeArr[2] = new CameraNode();
        cameraNodeArr[2].init(activity);
        cameraNodeArr[3] = new CameraNode();
        cameraNodeArr[3].init(activity);

        glSurfaceView.setEGLContextFactory(new GLSurfaceView.EGLContextFactory() {
            @Override
            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL10.EGL_NONE};

                EGLContext eglContext = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT,
                        2 != 0 ? attrib_list : null);

                glSurfaceView.setTag(eglContext);
                return eglContext;
            }

            @Override
            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                if (!egl.eglDestroyContext(display, context)) {
                    Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                }
            }
        });

        offScreenRenderObject = new DefaultRenderObject(activity);
        offScreenRenderObject.isBindFbo = true;
        offScreenRenderObject.isOES = false;

        screenRenderObject = new DefaultRenderObject(activity);
        screenRenderObject.isBindFbo = false;
        screenRenderObject.isOES = false;

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {

            int viewWidth, viewHeight;
            FpsUtil fpsUtil = new FpsUtil("4in1.draw");

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                offScreenRenderObject.onCreate();
                offScreenRenderObject.clearFlag = false;
                screenRenderObject.onCreate();
                cameraNodeArr[0].onSurfaceCreate(glSurfaceView, mPreviewSize);
                cameraNodeArr[1].onSurfaceCreate(glSurfaceView, mPreviewSize);
                cameraNodeArr[2].onSurfaceCreate(glSurfaceView, mPreviewSize);
                cameraNodeArr[3].onSurfaceCreate(glSurfaceView, mPreviewSize);

                try {
                    createCameraSession(camera[0], cameraNodeArr[0].getSurface());
                    createCameraSession(camera[1], cameraNodeArr[1].getSurface());
                    createCameraSession(camera[2], cameraNodeArr[2].getSurface());
                    createCameraSession(camera[3], cameraNodeArr[3].getSurface());
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                this.viewWidth = width;
                this.viewHeight = height;
                offScreenRenderObject.onChange(mPreviewSize.getWidth() * 2, mPreviewSize.getHeight() * 2);
                screenRenderObject.onChange(width, height);
                cameraNodeArr[0].onSurfaceChanged(gl, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                cameraNodeArr[1].onSurfaceChanged(gl, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                cameraNodeArr[2].onSurfaceChanged(gl, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                cameraNodeArr[3].onSurfaceChanged(gl, mPreviewSize.getWidth(), mPreviewSize.getHeight());
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                fpsUtil.trigger();
                cameraNodeArr[0].onDrawFrame(gl, 0, offScreenRenderObject, mPreviewSize.getWidth(), mPreviewSize.getHeight(), 0, 0);
                cameraNodeArr[1].onDrawFrame(gl, 1, offScreenRenderObject, mPreviewSize.getWidth(), mPreviewSize.getHeight(), 0, mPreviewSize.getHeight());
                cameraNodeArr[2].onDrawFrame(gl, 2, offScreenRenderObject, mPreviewSize.getWidth(), mPreviewSize.getHeight(), mPreviewSize.getWidth(), 0);
                cameraNodeArr[3].onDrawFrame(gl, 3, offScreenRenderObject, mPreviewSize.getWidth(), mPreviewSize.getHeight(), mPreviewSize.getWidth(), mPreviewSize.getHeight());

                screenRenderObject.onDraw(offScreenRenderObject.fboTextureId);

                if (videoEncodeRecode != null && videoEncodeRecode.isEncodeStart()) {
                    boolean hasAvailable = cameraNodeArr[0].frameAvailable || cameraNodeArr[1].frameAvailable
                            || cameraNodeArr[2].frameAvailable || cameraNodeArr[3].frameAvailable;

                    if (hasAvailable) {
                        videoEncodeRecode.requestRender();
                    }
                }
            }
        });

        glSurfaceView.setRenderMode(EglSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void createCameraSession(CameraDevice cameraDevice, Surface surface) throws CameraAccessException {
        CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(surface);

        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surface);

        cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2GLSurfaceViewConcatRecordManager.this.session = session;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        IOUtil.close(session);
        for (int i = 0; i < camera.length; i++) {
            IOUtil.close(camera[i]);
        }
        cameraHandlerThread.quitSafely();
    }

    public boolean isStart() {
        return videoEncodeRecode!= null && videoEncodeRecode.isEncodeStart();
    }

    public void startRecord() {
        if (videoEncodeRecode != null) {
            ToastUtil.show("正在录制中");
            return;
        }

        String cameraId = "all";
        videoEncodeRecode = new VideoRecordEncoder(activity, cameraId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        String fileName = "cameraId_" + cameraId + "_" + dateTime + ".mp4";
        savePath = activity.getExternalCacheDir().getAbsolutePath() + File.separator + fileName;
        ToastUtil.show("开始录制:" + cameraId);

        int videoOutputWidth, videoOutputHeight; //生成的视频文件的宽高
        videoOutputWidth = mPreviewSize.getWidth() * 1;
        videoOutputHeight = mPreviewSize.getHeight() * 1;

        FpsUtil fpsUtil = new FpsUtil("record.onDrawFrame()");

        videoEncodeRecode.initEncoder((EGLContext) glSurfaceView.getTag(), savePath,
                videoOutputWidth, videoOutputHeight, 44100, 2, 16);
        videoEncodeRecode.setRender(new EglSurfaceView.Renderer() {

            DefaultRenderObject record_DefaultRenderObject;

            @Override
            public void onSurfaceCreated() {
                record_DefaultRenderObject = new DefaultRenderObject(activity);
                record_DefaultRenderObject.isBindFbo = false;
                record_DefaultRenderObject.isOES = false;
                record_DefaultRenderObject.onCreate();
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                record_DefaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame() {
                fpsUtil.trigger();
                record_DefaultRenderObject.onDraw(offScreenRenderObject.fboTextureId);
            }
        });
        videoEncodeRecode.setRenderMode(VideoRecordEncoder.RENDERMODE_WHEN_DIRTY);
        videoEncodeRecode.startRecode();

        //////////////    开始录音    //////////////
        audioRecorder = new AudioRecorder();
        audioRecorder.setOnAudioDataArrivedListener(new AudioRecorder.OnAudioDataArrivedListener() {
            @Override
            public void onAudioDataArrived(byte[] audioData, int length) {
                if (videoEncodeRecode!= null && videoEncodeRecode.isEncodeStart()) {
                    videoEncodeRecode.putPcmData(audioData, length);
                }
            }
        });
        audioRecorder.startRecord();
    }

    public void stopRecord() {
        if (videoEncodeRecode != null && videoEncodeRecode.isEncodeStart()) {
            audioRecorder.stopRecord();
            videoEncodeRecode.stopRecode();
            videoEncodeRecode = null;
            ToastUtil.show("停止录制");
        } else {
            ToastUtil.show("请先开始录制");
        }
    }
}
