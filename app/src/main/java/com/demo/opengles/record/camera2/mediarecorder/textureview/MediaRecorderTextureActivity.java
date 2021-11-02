package com.demo.opengles.record.camera2.mediarecorder.textureview;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MediaRecorderTextureActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = MediaRecorderTextureActivity.class.getSimpleName();
    private TextureView mTextureview;
    private Button mBtnStart, mBtnFinish;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private Camera.Size mSelectSize;//记录当前选择的分辨率
    private boolean isRecorder = false;//用于判断当前是否在录制视频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediarecorder_camera1_texture);
        mTextureview = findViewById(R.id.textureview);
        mBtnStart = findViewById(R.id.btn_start_record);
        mBtnFinish = findViewById(R.id.btn_stop_record);
        mBtnStart.setOnClickListener(this);
        mBtnFinish.setOnClickListener(this);
        initTextureViewListener();
        initMediaRecorder();
    }

    /**
     * 初始化TextureView监听
     */
    private void initTextureViewListener() {
        mTextureview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) { //Textureview初始化启用回调
                initCamera();
                initCameraConfig();
                try {
                    mCamera.setPreviewTexture(surface);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_record:
                startRecorder();

                break;
            case R.id.btn_stop_record:
                stopRecorder();

                break;
            default:
                break;
        }
    }

    /**
     * 初始化MediaRecorder
     */
    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
    }

    /**
     * 选择摄像头
     *
     * @param isFacing true=前摄像头 false=后摄像头
     * @return 摄像id
     */
    private Integer selectCamera(boolean isFacing) {
        int cameraCount = Camera.getNumberOfCameras();
//        CameraInfo.CAMERA_FACING_BACK 后摄像头
//        CameraInfo.CAMERA_FACING_FRONT  前摄像头
        int facing = isFacing ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        Log.e(TAG, "selectCamera: cameraCount=" + cameraCount);
        if (cameraCount == 0) {
            Log.e(TAG, "selectCamera: The device does not have a camera ");
            return null;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == facing) {
                return i;
            }

        }
        return null;
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        mCamera = Camera.open(selectCamera(false));
        mSelectSize = selectPreviewSize(mCamera.getParameters());
    }

    /**
     * 初始化相机配置
     */
    private void initCameraConfig() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//关闭闪光灯
        parameters.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO); //对焦设置为自动
        parameters.setPreviewSize(mSelectSize.width, mSelectSize.height);//设置预览尺寸
        parameters.setPictureSize(mSelectSize.width, mSelectSize.height);//设置图片尺寸  就拿预览尺寸作为图片尺寸,其实他们基本上是一样的
        parameters.set("orientation", "portrait");//相片方向
        parameters.set("rotation", 90); //相片镜头角度转90度（默认摄像头是横拍）
//        mCamera.setParameters(parameters);//添加参数
        mCamera.setDisplayOrientation(90);//设置显示方向
    }

    /**
     * 计算获取预览尺寸
     *
     * @param parameters
     * @return
     */
    private Camera.Size selectPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        if (previewSizeList != null && previewSizeList.size() == 1) {
            return previewSizeList.get(0);
        }
        return null;
    }

    /**
     * 配置MedioRecorder
     */
    private void configMedioRecorder() {
        File saveRecorderFile = new File(getExternalCacheDir(), "CameraRecorder.mp4");
        if (saveRecorderFile.exists()) {
            saveRecorderFile.delete();
        }
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//设置音频源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);//设置视频源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//设置音频输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);//设置视频编码格式
        mMediaRecorder.setVideoSize(mSelectSize.width, mSelectSize.height);//设置视频分辨率
        mMediaRecorder.setVideoEncodingBitRate(8 * 1920 * 1080);//设置视频的比特率
        mMediaRecorder.setVideoFrameRate(60);//设置视频的帧率
        mMediaRecorder.setOrientationHint(90);//设置视频的角度
        mMediaRecorder.setMaxDuration(60 * 1000);//设置最大录制时间
        Surface surface = new Surface(mTextureview.getSurfaceTexture());
        mMediaRecorder.setPreviewDisplay(surface);//设置预览
        mMediaRecorder.setOutputFile(saveRecorderFile.getAbsolutePath());//设置文件保存路径
        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() { //录制异常监听
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                try {
                    mCamera.setPreviewTexture(mTextureview.getSurfaceTexture());
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 开启录制视频
     */
    private void startRecorder() {
        if (!isRecorder) {//如果不在录制视频
            mCamera.stopPreview();//暂停相机预览
            configMedioRecorder();//再次配置MedioRecorder
            try {
                mMediaRecorder.prepare();//准备录制
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaRecorder.start();//开始录制
            isRecorder = true;
        }
    }

    /**
     * 停止录制视频
     */
    private void stopRecorder() {
        if (isRecorder) { //如果在录制视频
            mMediaRecorder.stop();//暂停录制
            mMediaRecorder.reset();//重置,将MediaRecorder调整为空闲状态
            isRecorder = false;
            try {
                mCamera.setPreviewTexture(mTextureview.getSurfaceTexture());//重新设置预览SurfaceTexture
                mCamera.startPreview(); //重新开启相机预览
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    }

}
