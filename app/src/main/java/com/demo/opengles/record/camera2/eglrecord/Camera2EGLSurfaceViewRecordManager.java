package com.demo.opengles.record.camera2.eglrecord;

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
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class Camera2EGLSurfaceViewRecordManager {

    private Activity activity;
    private EglSurfaceView eglSurfaceView;
    private int cameraId;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private Size mPreviewSize; // 预览大小
    private CameraDevice camera;
    private CameraCaptureSession session;

    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;

    private int cameraTextureId;
    private SurfaceTexture cameraSurfaceTexture;
    private Surface surface;

    private CameraRenderObject cameraRenderObject;
    private WaterMarkRenderObject waterMarkRenderObject;
    private DefaultRenderObject defaultRenderObject;
    private DefaultFitRenderObject defaultFitRenderObject;

    private VideoRecordEncoder videoEncodeRecode;
    private AudioRecorder audioRecorder;

    private String savePath;

    public String getSavePath() {
        return savePath;
    }


    public void create(Activity activity, EglSurfaceView eglSurfaceView, int cameraId) {
        this.activity = activity;
        this.eglSurfaceView = eglSurfaceView;
        this.cameraId = cameraId;

        cameraRenderObject = new CameraRenderObject(activity);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        waterMarkRenderObject = new WaterMarkRenderObject(activity);
        waterMarkRenderObject.isBindFbo = true;
        waterMarkRenderObject.isOES = false;
        defaultRenderObject = new DefaultRenderObject(activity);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;
        defaultFitRenderObject = new DefaultFitRenderObject(activity);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;

        cameraHandlerThread = new HandlerThread("cameraOpenThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        eglSurfaceView.setRenderer(new EglSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated() {
                cameraTextureId = OpenGLESUtil.createOesTexture();
                cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);
                surface = new Surface(cameraSurfaceTexture);

                FpsUtil fpsUtil = new FpsUtil("camera2-onFrameAvailable" + cameraId);
                cameraSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        fpsUtil.trigger();
                        eglSurfaceView.requestRender();

                        if (videoEncodeRecode != null && videoEncodeRecode.isEncodeStart()) {
//                            TimeConsumeUtil.start("requestRender, " + cameraId);
                            videoEncodeRecode.requestRender();
//                            TimeConsumeUtil.calc("requestRender, " + cameraId);
                        }
                    }
                });

                cameraRenderObject.onCreate();
                waterMarkRenderObject.onCreate();
                defaultRenderObject.onCreate();
                defaultFitRenderObject.onCreate();

                try {
                    openCamera2();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                if (mPreviewSize != null) {
                    //设置Surface纹理的宽高，Camera2在预览时会选择宽高最相近的预览尺寸，将此尺寸的图像输送到Surface纹理中
                    cameraSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    cameraRenderObject.inputWidth = mPreviewSize.getWidth();
                    cameraRenderObject.inputHeight = mPreviewSize.getHeight();
                }
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                cameraRenderObject.onChange(width, height);
                waterMarkRenderObject.onChange(width, height);

//                cameraRenderObject.onChange(cameraRenderObject.inputHeight, cameraRenderObject.inputWidth);
//                waterMarkRenderObject.onChange(cameraRenderObject.inputHeight, cameraRenderObject.inputWidth);

                defaultRenderObject.onChange(width, height);

//                defaultFitRenderObject.inputWidth = waterMarkRenderObject.width;
//                defaultFitRenderObject.inputHeight = waterMarkRenderObject.height;
//                defaultFitRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame() {
                cameraSurfaceTexture.updateTexImage();
                cameraRenderObject.onDraw(cameraTextureId);
                waterMarkRenderObject.onDraw(cameraRenderObject.fboTextureId);
                defaultRenderObject.onDraw(waterMarkRenderObject.fboTextureId);
//                defaultFitRenderObject.onDraw(waterMarkRenderObject.fboTextureId);
            }
        });

        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @SuppressLint("MissingPermission")
    private void openCamera2() throws Exception {
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        characteristics = cameraManager.getCameraCharacteristics(cameraId + "");
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        //硬件层特征，为了让图像在手机屏幕直立显示时，需要将图像顺时针旋转此角度。不同手机厂商的此角度值会有不同
        cameraRenderObject.orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        cameraRenderObject.orientationEnable = true;
        //framework-sdk特性：这里获取到的size的宽高的方向是针对屏幕竖屏的上方向的宽高(此宽高与orientation无关)
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);

        Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());

        mPreviewSize = chooseOptimalSize(sizes, eglSurfaceView.getWidth(), eglSurfaceView.getHeight(), largest);
        mPreviewSize = sizes[0];

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2EGLSurfaceViewRecordManager.this.camera = camera;

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

    public boolean isStart() {
        if (videoEncodeRecode != null && videoEncodeRecode.isEncodeStart()) {
            return true;
        }
        return false;
    }

    public void startRecord() {
        if (videoEncodeRecode != null) {
            ToastUtil.show("正在录制中");
            return;
        }

        videoEncodeRecode = new VideoRecordEncoder(activity, cameraId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        String fileName = "cameraId" + cameraId + "_" + dateTime + ".mp4";
        savePath = activity.getExternalCacheDir().getAbsolutePath() + File.separator + fileName;
        ToastUtil.show("开始录制:" + cameraId);

        int videoOutputWidth, videoOutputHeight; //生成的视频文件的宽高
        videoOutputWidth = mPreviewSize.getWidth();
        videoOutputHeight = mPreviewSize.getHeight();

        if (cameraRenderObject.orientationEnable && (cameraRenderObject.orientation == 90 || cameraRenderObject.orientation == 270)) {
            videoOutputWidth = mPreviewSize.getHeight();
            videoOutputHeight = mPreviewSize.getWidth();
        }

        videoEncodeRecode.initEncoder(eglSurfaceView.getEglContext(), savePath,
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
                record_DefaultRenderObject.onDraw(waterMarkRenderObject.fboTextureId);
            }
        });
        videoEncodeRecode.setRenderMode(VideoRecordEncoder.RENDERMODE_WHEN_DIRTY);
        videoEncodeRecode.startRecode();

        /////// 开始录音
        audioRecorder = new AudioRecorder();
        audioRecorder.setOnAudioDataArrivedListener(new AudioRecorder.OnAudioDataArrivedListener() {
            @Override
            public void onAudioDataArrived(byte[] audioData, int length) {
                if (videoEncodeRecode.isEncodeStart()) {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private void createCameraSession() throws CameraAccessException {
        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(surface);

        camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Camera2EGLSurfaceViewRecordManager.this.session = session;

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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
