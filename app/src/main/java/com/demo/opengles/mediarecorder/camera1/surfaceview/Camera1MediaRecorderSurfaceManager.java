package com.demo.opengles.mediarecorder.camera1.surfaceview;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Camera1MediaRecorderSurfaceManager {
    private static final String TAG = "MRSM";

    private Activity activity;
    private int cameraId;
    private SurfaceView surfaceView;

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private Camera.Size mSelectSize;//记录当前选择的分辨率
    private boolean isRecorder = false;//用于判断当前是否在录制视频

    public Camera1MediaRecorderSurfaceManager(Activity activity, int cameraId, SurfaceView surfaceView) {
        this.activity = activity;
        this.cameraId = cameraId;
        this.surfaceView = surfaceView;

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mCamera = Camera.open(cameraId);
                mSelectSize = selectPreviewSize(mCamera.getParameters());

                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//关闭闪光灯
                parameters.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO); //对焦设置为自动
                parameters.setPreviewSize(mSelectSize.width, mSelectSize.height);//设置预览尺寸
                parameters.setPictureSize(mSelectSize.width, mSelectSize.height);//设置图片尺寸  就拿预览尺寸作为图片尺寸,其实他们基本上是一样的
                parameters.set("orientation", "portrait");//相片方向
                parameters.set("rotation", 90); //相片镜头角度转90度（默认摄像头是横拍）
                //        mCamera.setParameters(parameters);//添加参数
                mCamera.setDisplayOrientation(90);//设置显示方向

                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                } catch (IOException e) {
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
        mMediaRecorder = new MediaRecorder();
    }

    /**
     * 计算获取预览尺寸
     */
    private Camera.Size selectPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        if (previewSizeList != null && previewSizeList.size() == 1) {
            return previewSizeList.get(0);
        }
        return null;
    }

    /**
     * 配置MediaRecorder
     */
    private void configMediaRecorder() {
        File saveRecorderFile = new File(activity.getExternalCacheDir(), "CameraRecorder_" + cameraId + ".mp4");
        if (saveRecorderFile.exists()) {
            saveRecorderFile.delete();
        }
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//设置音频源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);//设置视频源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//设置音频输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置视频编码格式
        mMediaRecorder.setVideoSize(mSelectSize.width, mSelectSize.height);//设置视频分辨率
//        mMediaRecorder.setVideoEncodingBitRate(8 * mSelectSize.width * mSelectSize.height);//设置视频的比特率
        mMediaRecorder.setVideoEncodingBitRate(1000000);
//        mMediaRecorder.setVideoFrameRate(25);//设置视频的帧率，这个设置有可能会出问题，有的手机不支持这种帧率就会录制失败，这里使用默认的帧率，当然视频的大小肯定会受影响
        mMediaRecorder.setOrientationHint(90);//设置视频的角度
        mMediaRecorder.setMaxDuration(60 * 1000);//设置最大录制时间
        mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());//设置预览
        mMediaRecorder.setOutputFile(saveRecorderFile.getAbsolutePath());//设置文件保存路径
        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() { //录制异常监听
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                try {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mCamera.setPreviewDisplay(surfaceView.getHolder());
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "onError, cameraId=" + cameraId + e.getMessage());
                }
            }
        });
    }

    /**
     * 开启录制视频
     */
    public void startRecorder() {
        try {
            if (!isRecorder) {//如果不在录制视频
                mCamera.stopPreview();//暂停相机预览
                configMediaRecorder();//再次配置MedioRecorder
                try {
                    mMediaRecorder.prepare();//准备录制
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMediaRecorder.start();//开始录制
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
                mCamera.startPreview(); //重新开启相机预览
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                    }
                });
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
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy, cameraId=" + cameraId + e.getMessage());
        }
    }

}