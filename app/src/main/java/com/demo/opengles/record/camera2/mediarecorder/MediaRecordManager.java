package com.demo.opengles.record.camera2.mediarecorder;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.demo.opengles.util.CollectUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MediaRecordManager {

    private static final String TAG = "Cannot invoke method length() on null object";

    private Activity activity;
    private int cameraId;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private MediaRecorder mediaRecorder;
    private Camera camera;

    public MediaRecordManager(Activity activity, int cameraId, SurfaceView surfaceView) {
        this.activity = activity;
        this.cameraId = cameraId;
        this.surfaceView = surfaceView;

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                openCamera();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
    }

    private void openCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            return;
        }
        int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        camera = Camera.open(cameraId);

        int degree = computeDegrees(cameraId);
        Log.e(TAG, "相机预览在此界面显示时，需要旋转的角度 = " + degree);
        camera.setDisplayOrientation(degree);

        //设置相机参数
        Camera.Parameters parameters = camera.getParameters();
        //系统特性：拍照的聚焦频率要高于拍视频
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(100);
        CollectUtil.execute(parameters.getSupportedPreviewFormats(),
                new CollectUtil.Executor<Integer>() {
                    @Override
                    public void execute(Integer integer) {
                        Log.e(TAG, "SupportedPreviewFormats: integer = " + integer);
                    }
                });

        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        CollectUtil.execute(sizeList, new CollectUtil.Executor<Camera.Size>() {
            @Override
            public void execute(Camera.Size size) {
                Log.e(TAG, "SupportedPreviewSizes: size.width = "
                        + size.width + " , size.height = " + size.height);
            }
        });
        Camera.Size size = adjustSurfaceViewWidthHeight(sizeList);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    public void startRecord() {
        camera.unlock();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        String srcPath = activity.getExternalCacheDir().getAbsolutePath();
        String srcName = System.currentTimeMillis() + ".mp4";

        File mRecorderFile = new File(srcPath + srcName);

        mediaRecorder.setOutputFile(mRecorderFile.getAbsolutePath());

        mediaRecorder.setCamera(camera);
//        mediaRecorder.setOrientationHint(mOrientation);
        //从麦克风采集
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile mCamcorderProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
//        System.out.println("============mCamcorderProfile============" + mCamcorderProfile.videoFrameWidth + "   " + mCamcorderProfile.videoFrameHeight);
        mediaRecorder.setProfile(mCamcorderProfile);
        //使用CamcorderProfile做配置的话，输出格式，音频编码，视频编码 不要写,否则会报错（崩溃）
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        //设置录制视频的大小，其实Camera也必须要和这个比例相同，此处为了简单不做处理
//        mediaRecorder.setVideoSize(mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);
        //提高帧频率，录像模糊，花屏，绿屏可写上调试
//        mediaRecorder.setVideoEncodingBitRate(mCamcorderProfile.videoFrameWidth * mCamcorderProfile.videoFrameHeight * 24 * 16);
//        mediaRecorder.setVideoFrameRate(24);
        //所有android系统都支持的适中采样的频率
//        mediaRecorder.setAudioSamplingRate(44100);

        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        //开始录音
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    //根据屏幕的旋转角度、相机的硬件内置放置角度，来设置显示旋转角度
    private int computeDegrees(int cameraId) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Log.e(TAG, "DefaultDisplay.Rotation = " + rotation);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //前置相机，算法由谷歌提供
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //后置相机
            result = (cameraInfo.orientation - degrees) % 360;
        } else {
            //未知方向的相机，通常安卓手机上只有前后两个摄像头，其它智能设备如车机可能存在多个摄像头
            result = (cameraInfo.orientation - degrees) % 360;
        }

        return result;
    }

    /**
     * 相机硬件支持一系列固定的宽高的图像预览数据，而屏幕View可以被开发者设置成任意宽高，
     * 那么必然存在预览数据宽高比例与界面View的宽高比例不相等，若要保持View不变，则必然存在图像比例变形。
     * <p>
     * 预览比例与View比例的选择理论依据：
     * 1、在保持界面View完全填充的情况，尽量选择宽高比接近View的预览宽高比，允许弱微的变形
     */
    private Camera.Size adjustSurfaceViewWidthHeight(List<Camera.Size> sizeList) {
        float ASPECT_TOLERANCE = 0.1f;

        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);

            int cameraWidth = Math.min(size.width, size.height);
            int cameraHeight = Math.max(size.width, size.height);
            float cameraSizeScale = (float) cameraWidth / cameraHeight;
            float surfaceViewSizeScale = (float) surfaceView.getWidth() / surfaceView.getHeight();

            if (Math.abs(cameraSizeScale - surfaceViewSizeScale) < ASPECT_TOLERANCE) {
                Log.e(TAG, "按顺序查找，找到比例小于阈值的预览size =" + size.width + ", " + size.height);
                return size;
            }
        }

        int minHeightDiff = Integer.MAX_VALUE;
        Camera.Size targetSize = null;
        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);

            int cameraWidth = Math.min(size.width, size.height);
            int cameraHeight = Math.max(size.width, size.height);

            int diff = Math.abs(surfaceView.getHeight() - cameraHeight);
            if (diff < minHeightDiff) {
                targetSize = size;
                minHeightDiff = diff;
            }
        }

        Log.e(TAG, "找到高度差值最小的size =" + targetSize.width + ", " + targetSize.height);
        return targetSize;
    }

}
