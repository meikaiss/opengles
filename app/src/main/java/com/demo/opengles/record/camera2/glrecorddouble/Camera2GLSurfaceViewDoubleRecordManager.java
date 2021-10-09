package com.demo.opengles.record.camera2.glrecorddouble;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultFitRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.gaussian.render.WaterMarkRenderObject;
import com.demo.opengles.record.camera1.AudioRecorder;
import com.demo.opengles.record.camera1.VideoRecordEncoder;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.FpsUtil;
import com.demo.opengles.util.IOUtil;
import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class Camera2GLSurfaceViewDoubleRecordManager {

    private Activity activity;
    private ViewGroup parent1;
    private ViewGroup parent2;
    private GLSurfaceView glSurfaceView1;
    private GLSurfaceView glSurfaceView2;
    private int cameraId;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private Size mPreviewSize; // 预览大小
    private CameraDevice camera;
    private CameraCaptureSession session;
    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private int cameraTextureId1;
    private SurfaceTexture cameraSurfaceTexture1;
    private Surface surface1;
    private boolean hasSurface1Prepare; //标记Surface相关的资源是否已准备好，包括对象创建、设置纹理宽高

    private CameraRenderObject cameraRenderObject1;
    private WaterMarkRenderObject waterMarkRenderObject1;
    private DefaultFitRenderObject defaultFitRenderObject1;

    private VideoRecordEncoder videoEncodeRecode1;
    private AudioRecorder audioRecorder1;

    private String savePath1;

    //////////////////////////////////////////////////////////////////////////////

    private int cameraTextureId2;
    private SurfaceTexture cameraSurfaceTexture2;
    private Surface surface2;
    private boolean hasSurface2Prepare; //标记Surface相关的资源是否已准备好，包括对象创建、设置纹理宽高

    private CameraRenderObject cameraRenderObject2;
    private WaterMarkRenderObject waterMarkRenderObject2;
    private DefaultFitRenderObject defaultFitRenderObject2;

    private VideoRecordEncoder videoEncodeRecode2;
    private AudioRecorder audioRecorder2;

    private String savePath2;

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    public String getSavePath1() {
        return savePath1;
    }

    public String getSavePath2() {
        return savePath2;
    }

    public void create(Activity activity, GLSurfaceView glSurfaceView1, GLSurfaceView glSurfaceView2, int cameraId) {
        this.activity = activity;
        this.glSurfaceView1 = glSurfaceView1;
        this.glSurfaceView2 = glSurfaceView2;
        this.cameraId = cameraId;

        parent1 = (ViewGroup) glSurfaceView1.getParent();
        parent1.removeView(glSurfaceView1);
        parent2 = (ViewGroup) glSurfaceView2.getParent();
        parent2.removeView(glSurfaceView2);

        cameraHandlerThread = new HandlerThread("cameraOpenThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        try {
            openCamera2();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGlSurfaceView1() {
        parent1.addView(glSurfaceView1);
        glSurfaceView1.setEGLContextClientVersion(2);

        cameraRenderObject1 = new CameraRenderObject(activity);
        cameraRenderObject1.isBindFbo = true;
        cameraRenderObject1.isOES = true;
        waterMarkRenderObject1 = new WaterMarkRenderObject(activity);
        waterMarkRenderObject1.isBindFbo = true;
        waterMarkRenderObject1.isOES = false;
        defaultFitRenderObject1 = new DefaultFitRenderObject(activity);
        defaultFitRenderObject1.isBindFbo = false;
        defaultFitRenderObject1.isOES = false;

        glSurfaceView1.setEGLContextFactory(new GLSurfaceView.EGLContextFactory() {
            @Override
            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL10.EGL_NONE};

                EGLContext eglContext = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT,
                        2 != 0 ? attrib_list : null);

                glSurfaceView1.setTag(eglContext);
                return eglContext;
            }

            @Override
            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                if (!egl.eglDestroyContext(display, context)) {
                    Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                }
            }
        });

        glSurfaceView1.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                cameraTextureId1 = OpenGLESUtil.createOesTexture();
                cameraSurfaceTexture1 = new SurfaceTexture(cameraTextureId1);
                surface1 = new Surface(cameraSurfaceTexture1);

                FpsUtil fpsUtil = new FpsUtil("onFrameAvailable, id=" + cameraId);

                cameraSurfaceTexture1.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                        fpsUtil.trigger();

                        glSurfaceView1.requestRender();

                        if (videoEncodeRecode1 != null && videoEncodeRecode1.isEncodeStart()) {
                            videoEncodeRecode1.requestRender();
                        }
                    }
                });

                cameraRenderObject1.onCreate();
                waterMarkRenderObject1.onCreate();
                defaultFitRenderObject1.onCreate();

                //设置Surface纹理的宽高，Camera2在预览时会选择宽高最相近的预览尺寸，将此尺寸的图像输送到Surface纹理中
                cameraSurfaceTexture1.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                cameraRenderObject1.inputWidth = mPreviewSize.getWidth();
                cameraRenderObject1.inputHeight = mPreviewSize.getHeight();

                hasSurface1Prepare = true;
                startCameraSession();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                cameraRenderObject1.onChange(cameraRenderObject1.inputWidth, cameraRenderObject1.inputHeight);
                waterMarkRenderObject1.onChange(cameraRenderObject1.inputWidth, cameraRenderObject1.inputHeight);

                defaultFitRenderObject1.inputWidth = waterMarkRenderObject1.width;
                defaultFitRenderObject1.inputHeight = waterMarkRenderObject1.height;
                defaultFitRenderObject1.onChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                cameraSurfaceTexture1.updateTexImage();
                cameraRenderObject1.onDraw(cameraTextureId1);
                waterMarkRenderObject1.onDraw(cameraRenderObject1.fboTextureId);
                defaultFitRenderObject1.onDraw(waterMarkRenderObject1.fboTextureId);
            }
        });

        glSurfaceView1.setRenderMode(EglSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void initGlSurfaceView2() {
        parent2.addView(glSurfaceView2);
        glSurfaceView2.setEGLContextClientVersion(2);

        cameraRenderObject2 = new CameraRenderObject(activity);
        cameraRenderObject2.isBindFbo = true;
        cameraRenderObject2.isOES = true;
        waterMarkRenderObject2 = new WaterMarkRenderObject(activity);
        waterMarkRenderObject2.isBindFbo = true;
        waterMarkRenderObject2.isOES = false;
        defaultFitRenderObject2 = new DefaultFitRenderObject(activity);
        defaultFitRenderObject2.isBindFbo = false;
        defaultFitRenderObject2.isOES = false;

        glSurfaceView2.setEGLContextFactory(new GLSurfaceView.EGLContextFactory() {
            @Override
            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL10.EGL_NONE};

                EGLContext eglContext = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT,
                        2 != 0 ? attrib_list : null);

                glSurfaceView2.setTag(eglContext);
                return eglContext;
            }

            @Override
            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                if (!egl.eglDestroyContext(display, context)) {
                    Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                }
            }
        });

        glSurfaceView2.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                cameraTextureId2 = OpenGLESUtil.createOesTexture();
                cameraSurfaceTexture2 = new SurfaceTexture(cameraTextureId2);
                surface2 = new Surface(cameraSurfaceTexture2);

                FpsUtil fpsUtil = new FpsUtil("onFrameAvailable, id=" + cameraId);

                cameraSurfaceTexture2.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                        fpsUtil.trigger();

                        glSurfaceView2.requestRender();

                        if (videoEncodeRecode2 != null && videoEncodeRecode2.isEncodeStart()) {
                            videoEncodeRecode2.requestRender();
                        }
                    }
                });

                cameraRenderObject2.onCreate();
                waterMarkRenderObject2.onCreate();
                defaultFitRenderObject2.onCreate();

                //设置Surface纹理的宽高，Camera2在预览时会选择宽高最相近的预览尺寸，将此尺寸的图像输送到Surface纹理中
                cameraSurfaceTexture2.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                cameraRenderObject2.inputWidth = mPreviewSize.getWidth();
                cameraRenderObject2.inputHeight = mPreviewSize.getHeight();

                hasSurface2Prepare = true;
                startCameraSession();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                cameraRenderObject2.onChange(cameraRenderObject2.inputWidth, cameraRenderObject2.inputHeight);
                waterMarkRenderObject2.onChange(cameraRenderObject2.inputWidth, cameraRenderObject2.inputHeight);

                defaultFitRenderObject2.inputWidth = waterMarkRenderObject2.width;
                defaultFitRenderObject2.inputHeight = waterMarkRenderObject2.height;
                defaultFitRenderObject2.onChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                cameraSurfaceTexture2.updateTexImage();
                cameraRenderObject2.onDraw(cameraTextureId2);
                waterMarkRenderObject2.onDraw(cameraRenderObject2.fboTextureId);
                defaultFitRenderObject2.onDraw(waterMarkRenderObject2.fboTextureId);
            }
        });

        glSurfaceView2.setRenderMode(EglSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @SuppressLint("MissingPermission")
    private void openCamera2() throws Exception {
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        characteristics = cameraManager.getCameraCharacteristics(cameraId + "");
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
        mPreviewSize = chooseOptimalSize(sizes, glSurfaceView1.getWidth(), glSurfaceView1.getHeight(), largest);
        mPreviewSize = sizes[0]; //直接选择最大的预览尺寸

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2GLSurfaceViewDoubleRecordManager.this.camera = camera;


                initGlSurfaceView1();
                initGlSurfaceView2();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }, cameraThreadHandler);

    }

    public boolean isStart() {
        if (videoEncodeRecode1 != null && videoEncodeRecode1.isEncodeStart()) {
            return true;
        }
        return false;
    }

    public void startRecord() {
        startRecord1();
        startRecord2();
    }

    public void stopRecord() {
        stopRecord1();
        stopRecord2();
    }

    public void startRecord1() {
        if (videoEncodeRecode1 != null) {
            ToastUtil.show("正在录制中");
            return;
        }

        videoEncodeRecode1 = new VideoRecordEncoder(activity, cameraId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        String fileName = "cameraId" + cameraId + "_t1_" + dateTime + ".mp4";
        savePath1 = activity.getExternalCacheDir().getAbsolutePath() + File.separator + fileName;
        ToastUtil.show("开始录制:" + cameraId);

        int videoOutputWidth, videoOutputHeight; //生成的视频文件的宽高
        videoOutputWidth = mPreviewSize.getWidth();
        videoOutputHeight = mPreviewSize.getHeight();

        if (cameraRenderObject1.orientationEnable && (cameraRenderObject1.orientation == 90 || cameraRenderObject1.orientation == 270)) {
            videoOutputWidth = mPreviewSize.getHeight();
            videoOutputHeight = mPreviewSize.getWidth();
        }

        videoEncodeRecode1.initEncoder((EGLContext) glSurfaceView1.getTag(), savePath1,
                videoOutputWidth, videoOutputHeight, 44100, 2, 16);
        videoEncodeRecode1.setRender(new EglSurfaceView.Renderer() {

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
                record_DefaultRenderObject.onDraw(waterMarkRenderObject1.fboTextureId);
            }
        });
        videoEncodeRecode1.setRenderMode(VideoRecordEncoder.RENDERMODE_WHEN_DIRTY);
        videoEncodeRecode1.startRecode();

        /////// 开始录音
        audioRecorder1 = new AudioRecorder();
        audioRecorder1.setOnAudioDataArrivedListener(new AudioRecorder.OnAudioDataArrivedListener() {
            @Override
            public void onAudioDataArrived(byte[] audioData, int length) {
                if (videoEncodeRecode1.isEncodeStart()) {
                    videoEncodeRecode1.putPcmData(audioData, length);
                }
            }
        });
        audioRecorder1.startRecord();
    }

    public void startRecord2() {
        if (videoEncodeRecode2 != null) {
            ToastUtil.show("正在录制中");
            return;
        }

        videoEncodeRecode2 = new VideoRecordEncoder(activity, cameraId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        String fileName = "cameraId" + cameraId + "_t2_" + dateTime + ".mp4";
        savePath2 = activity.getExternalCacheDir().getAbsolutePath() + File.separator + fileName;
        ToastUtil.show("开始录制:" + cameraId);

        int videoOutputWidth, videoOutputHeight; //生成的视频文件的宽高
        videoOutputWidth = mPreviewSize.getWidth();
        videoOutputHeight = mPreviewSize.getHeight();

        if (cameraRenderObject1.orientationEnable && (cameraRenderObject1.orientation == 90 || cameraRenderObject1.orientation == 270)) {
            videoOutputWidth = mPreviewSize.getHeight();
            videoOutputHeight = mPreviewSize.getWidth();
        }

        videoEncodeRecode2.initEncoder((EGLContext) glSurfaceView1.getTag(), savePath2,
                videoOutputWidth, videoOutputHeight, 44100, 2, 16);
        videoEncodeRecode2.setRender(new EglSurfaceView.Renderer() {

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
                record_DefaultRenderObject.onDraw(waterMarkRenderObject1.fboTextureId);
            }
        });
        videoEncodeRecode2.setRenderMode(VideoRecordEncoder.RENDERMODE_WHEN_DIRTY);
        videoEncodeRecode2.startRecode();

        /////// 开始录音
        audioRecorder2 = new AudioRecorder();
        audioRecorder2.setOnAudioDataArrivedListener(new AudioRecorder.OnAudioDataArrivedListener() {
            @Override
            public void onAudioDataArrived(byte[] audioData, int length) {
                if (videoEncodeRecode2.isEncodeStart()) {
                    videoEncodeRecode2.putPcmData(audioData, length);
                }
            }
        });
        audioRecorder2.startRecord();
    }

    public void stopRecord1() {
        if (videoEncodeRecode1 != null && videoEncodeRecode1.isEncodeStart()) {
            audioRecorder1.stopRecord();
            videoEncodeRecode1.stopRecode();
            videoEncodeRecode1 = null;
            ToastUtil.show("停止录制");
        } else {
            ToastUtil.show("请先开始录制");
        }
    }

    public void stopRecord2() {
        if (videoEncodeRecode2 != null && videoEncodeRecode2.isEncodeStart()) {
            audioRecorder2.stopRecord();
            videoEncodeRecode2.stopRecode();
            videoEncodeRecode2 = null;
            ToastUtil.show("停止录制");
        } else {
            ToastUtil.show("请先开始录制");
        }
    }

    private void startCameraSession() {
        if (hasSurface1Prepare && hasSurface2Prepare) {
            try {
                createCameraSession();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private volatile boolean hasCreateCameraSession = false;

    private void createCameraSession() throws CameraAccessException {
        if (hasCreateCameraSession) {
            return;
        }

        hasCreateCameraSession = true;
        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(surface1);
        builder.addTarget(surface2);

        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surface1);
        surfaceList.add(surface2);

        camera.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2GLSurfaceViewDoubleRecordManager.this.session = session;

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
        IOUtil.close(camera);
        cameraHandlerThread.quitSafely();
    }

    ////////////////////////////////////////////////////////////

    private Size chooseOptimalSize(Size[] sizes, int viewWidth, int viewHeight, Size pictureSize) {
        int totalRotation = getRotation();
        boolean swapRotation = totalRotation == 90 || totalRotation == 270;
        int width = swapRotation ? viewHeight : viewWidth;
        int height = swapRotation ? viewWidth : viewHeight;
        return getSuitableSize(sizes, width, height, pictureSize);
    }

    private int getRotation() {
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (displayRotation) {
            case Surface.ROTATION_0:
                displayRotation = 90;
                break;
            case Surface.ROTATION_90:
                displayRotation = 0;
                break;
            case Surface.ROTATION_180:
                displayRotation = 270;
                break;
            case Surface.ROTATION_270:
                displayRotation = 180;
                break;
        }
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int mDisplayRotate = (displayRotation + sensorOrientation + 270) % 360;
        return mDisplayRotate;
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

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}
