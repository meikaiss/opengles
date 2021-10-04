package com.demo.opengles.record.camera1;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.gaussian.render.WaterMarkRenderObject;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.CollectUtil;
import com.demo.opengles.util.FpsUtil;
import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Camera1RecordManager {
    private static final String TAG = "RecordManager";

    private Context context;
    private Camera camera;
    private int cameraId;
    private EglSurfaceView eglSurfaceView;

    private int cameraTextureId;
    private SurfaceTexture cameraSurfaceTexture;

    private CameraRenderObject cameraRenderObject;
    private WaterMarkRenderObject waterMarkRenderObject;
    private DefaultRenderObject defaultRenderObject;

    private VideoRecordEncoder videoEncodeRecode;
    private AudioRecorder audioRecorder;

    private String savePath;

    public String getSavePath() {
        return savePath;
    }

    public void onDestroy() {
        if (camera != null) {
            camera.release();
        }
    }

    public void create(Context context, EglSurfaceView eglSurfaceView, int cameraId) {
        this.context = context;
        this.eglSurfaceView = eglSurfaceView;
        this.cameraId = cameraId;

        cameraRenderObject = new CameraRenderObject(context);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        waterMarkRenderObject = new WaterMarkRenderObject(context);
        waterMarkRenderObject.isBindFbo = true;
        waterMarkRenderObject.isOES = false;
        defaultRenderObject = new DefaultRenderObject(context);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;

        eglSurfaceView.setRenderer(new EglSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated() {
                TimeConsumeUtil.start("Renderer" + cameraId);
                cameraTextureId = OpenGLESUtil.createOesTexture();

                cameraRenderObject.onCreate();
                waterMarkRenderObject.onCreate();
                defaultRenderObject.onCreate();

                cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);

                try {
                    initCamera1(cameraSurfaceTexture);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                TimeConsumeUtil.calc("Renderer", "onSurfaceCreated");
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                cameraRenderObject.onChange(width, height);
                waterMarkRenderObject.onChange(width, height);
                defaultRenderObject.onChange(width, height);

//                TimeConsumeUtil.calc("Renderer", "onSurfaceChanged");
            }

            @Override
            public void onDrawFrame() {
//                TimeConsumeUtil.direct("onDrawFrame, fps");
//                TimeConsumeUtil.start("onDrawFrame");
                cameraSurfaceTexture.updateTexImage();
                cameraRenderObject.onDraw(cameraTextureId);
                waterMarkRenderObject.onDraw(cameraRenderObject.fboTextureId);
                defaultRenderObject.onDraw(waterMarkRenderObject.fboTextureId);

//                TimeConsumeUtil.calc("onDrawFrame");
            }

        });
        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void initCamera1(SurfaceTexture surfaceTexture) throws Exception {
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            return;
        }

        FpsUtil fpsUtil = new FpsUtil("onFrameAvailable" + cameraId);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                fpsUtil.trigger();

//                TimeConsumeUtil.calc("onFrameAvailable" + cameraId);

                if (videoEncodeRecode != null && videoEncodeRecode.isEncodeStart()) {
//                    TimeConsumeUtil.start("requestRender, " + cameraId);
                    videoEncodeRecode.requestRender();
//                    TimeConsumeUtil.calc("requestRender, " + cameraId);
                }
            }
        });

        camera = Camera.open(cameraId);

        //设置相机参数
        Camera.Parameters parameters = camera.getParameters();
        //系统特性：拍照的聚焦频率要高于拍视频
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        parameters.setPictureFormat(ImageFormat.JPEG);
//        parameters.setJpegQuality(100);
        CollectUtil.execute(parameters.getSupportedPreviewFormats(),
                new CollectUtil.Executor<Integer>() {
                    @Override
                    public void execute(Integer integer) {
                    }
                });

        //在使用正交投影变换的情况下，不需要考虑图像宽高比与View宽高比不一致的问题，因为正交投影会保持图像原有的宽高比，允许上下或两侧出现空白
        //所以直接选择最清晰的预览尺寸
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = sizeList.get(0);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        Camera.Size pictureSize = pictureSizeList.get(0);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);

        camera.setParameters(parameters);

        cameraRenderObject.inputWidth = previewSize.width;
        cameraRenderObject.inputHeight = previewSize.height;

        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    public void startRecord() {

        /////// 开始录图像
        videoEncodeRecode = new VideoRecordEncoder(context, cameraId);
        videoEncodeRecode.setRender(new EglSurfaceView.Renderer() {

            DefaultRenderObject defaultRenderObject;

            @Override
            public void onSurfaceCreated() {
                defaultRenderObject = new DefaultRenderObject(context);
                defaultRenderObject.onCreate();
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                defaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame() {
                defaultRenderObject.onDraw(waterMarkRenderObject.fboTextureId);
            }
        });
        videoEncodeRecode.setRenderMode(VideoRecordEncoder.RENDERMODE_WHEN_DIRTY);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        String fileName = "cameraId" + cameraId + "_" + dateTime + ".mp4";
        savePath = context.getExternalCacheDir().getAbsolutePath() + File.separator + fileName;
        ToastUtil.show("开始录制:" + cameraId);

        videoEncodeRecode.initEncoder(eglSurfaceView.getEglContext(), savePath,
                cameraRenderObject.inputWidth, cameraRenderObject.inputHeight, 44100, 2, 16);
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
        audioRecorder.stopRecord();
        videoEncodeRecode.stopRecode();
        videoEncodeRecode = null;
        ToastUtil.show("停止录制");
    }

}
