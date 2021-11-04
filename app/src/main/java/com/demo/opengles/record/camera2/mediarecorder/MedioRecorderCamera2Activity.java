package com.demo.opengles.record.camera2.mediarecorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MedioRecorderCamera2Activity extends AppCompatActivity {
    private static final String TAG = MedioRecorderCamera2Activity.class.getSimpleName();
    private Button mBtnStatr, mBtnFinish;
    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice.StateCallback mCameraDeviceStateCallback;
    private CameraCaptureSession.StateCallback mSessionStateCallback;
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback;
    private CaptureRequest.Builder mPreviewCaptureRequest;
    private CaptureRequest.Builder mRecorderCaptureRequest;
    private MediaRecorder mMediaRecorder;
    private String mCurrentSelectCamera;
    private Handler mChildHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medio_recorder_camera2);
        mTextureView = findViewById(R.id.textureview);
        mBtnStatr = findViewById(R.id.btn_start);
        mBtnFinish = findViewById(R.id.btn_finish);
        initClickListener();
        initChildHandler();
        initTextureViewStateListener();
        initMediaRecorder();
        initCameraDeviceStateCallback();
        initSessionStateCallback();
        initSessionCaptureCallback();
    }

    private void initClickListener() {
        mBtnStatr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config();
                startRecorder();

            }
        });
        mBtnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecorder();

            }
        });
    }

    /**
     * 初始化TextureView的纹理生成监听，只有纹理生成准备好了。我们才能去进行摄像头的初始化工作让TextureView接收摄像头预览画面
     */
    private void initTextureViewStateListener() {
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //可以使用纹理
                initCameraManager();
                selectCamera();
                openCamera();

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                //纹理尺寸变化

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //纹理被销毁
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //纹理更新

            }
        });
    }

    /**
     * 初始化子线程Handler，操作Camera2需要一个子线程的Handler
     */
    private void initChildHandler() {
        HandlerThread handlerThread = new HandlerThread("Camera2Demo");
        handlerThread.start();
        mChildHandler = new Handler(handlerThread.getLooper());
    }

    /**
     * 初始化MediaRecorder
     */
    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
    }

    /**
     * 配置录制视频相关数据
     */
    private void configMediaRecorder() {
        File file = new File(getExternalCacheDir(), "demo.mp4");
        if (file.exists()) {
            file.delete();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频来源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//设置视频来源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//设置输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择AAC
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);//设置视频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择H264
        mMediaRecorder.setVideoEncodingBitRate(8 * 1024 * 1920);//设置比特率 一般是 1*分辨率 到 10*分辨率 之间波动。比特率越大视频越清晰但是视频文件也越大。
        mMediaRecorder.setVideoFrameRate(30);//设置帧数 选择 30即可， 过大帧数也会让视频文件更大当然也会更流畅，但是没有多少实际提升。人眼极限也就30帧了。
        Size size = getMatchingSize2();
        mMediaRecorder.setVideoSize(size.getWidth(), size.getHeight());
        mMediaRecorder.setOrientationHint(0);
        Surface surface = new Surface(mTextureView.getSurfaceTexture());
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 重新配置录制视频时的CameraCaptureSession
     */
    private void config() {
        mCameraCaptureSession.close();//关闭预览的会话，需要重新创建录制视频的会话
        mCameraCaptureSession = null;

        configMediaRecorder();
        Size cameraSize = getMatchingSize2();
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        Surface recorderSurface = mMediaRecorder.getSurface();//从获取录制视频需要的Surface
        try {
            mPreviewCaptureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//            mPreviewCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewCaptureRequest.addTarget(previewSurface);
            mPreviewCaptureRequest.addTarget(recorderSurface);
            //请注意这里设置了Arrays.asList(previewSurface,recorderSurface) 2个Surface，很好理解录制视频也需要有画面预览，第一个是预览的Surface，第二个是录制视频使用的Surface
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recorderSurface), mSessionStateCallback, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 开始录制视频
     */
    private void startRecorder() {
        mMediaRecorder.start();


    }

    /**
     * 暂停录制视频（暂停后视频文件会自动保存）
     */
    private void stopRecorder() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
    }

    /**
     * 初始化Camera2的相机管理，CameraManager用于获取摄像头分辨率，摄像头方向，摄像头id与打开摄像头的工作
     */
    private void initCameraManager() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    }

    /**
     * 选择一颗我们需要使用的摄像头，主要是选择使用前摄还是后摄或者是外接摄像头
     */
    private void selectCamera() {
        if (mCameraManager != null) {
            Log.e(TAG, "selectCamera: CameraManager is null");

        }
        try {
            String[] cameraIdList = mCameraManager.getCameraIdList();   //获取当前设备的全部摄像头id集合
            if (cameraIdList.length == 0) {
                Log.e(TAG, "selectCamera: cameraIdList length is 0");
            }
            for (String cameraId : cameraIdList) { //遍历所有摄像头
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);//得到当前id的摄像头描述特征
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING); //获取摄像头的方向特征信息
                if (facing == CameraCharacteristics.LENS_FACING_BACK) { //这里选择了后摄像头
                    mCurrentSelectCamera = cameraId;

                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initCameraDeviceStateCallback() {
        mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                //摄像头被打开
                try {
                    mCameraDevice = camera;
                    Size cameraSize = getMatchingSize2();//计算获取需要的摄像头分辨率
                    SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();//得到纹理
                    surfaceTexture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
                    Surface previewSurface = new Surface(surfaceTexture);
                    mPreviewCaptureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                    mPreviewCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    mPreviewCaptureRequest.addTarget(previewSurface);
                    mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), mSessionStateCallback, mChildHandler);//创建数据捕获会话，用于摄像头画面预览，这里需要等待mSessionStateCallback回调
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                //摄像头断开

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                //异常

            }
        };
    }

    private void initSessionStateCallback() {
        mSessionStateCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                mCameraCaptureSession = session;
                try {
                    //执行重复获取数据请求，等于一直获取数据呈现预览画面，mSessionCaptureCallback会返回此次操作的信息回调
                    mCameraCaptureSession.setRepeatingRequest(mPreviewCaptureRequest.build(), mSessionCaptureCallback, mChildHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        };
    }

    private void initSessionCaptureCallback() {
        mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
            }
        };
    }

    /**
     * 打开摄像头，这里打开摄像头后，我们需要等待mCameraDeviceStateCallback的回调
     */
    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            mCameraManager.openCamera(mCurrentSelectCamera, mCameraDeviceStateCallback, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算需要的使用的摄像头分辨率
     *
     * @return
     */
    private Size getMatchingSize2() {
        Size selectSize = null;
        try {
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCurrentSelectCamera);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics(); //因为我这里是将预览铺满屏幕,所以直接获取屏幕分辨率
            int deviceWidth = displayMetrics.widthPixels; //屏幕分辨率宽
            int deviceHeigh = displayMetrics.heightPixels; //屏幕分辨率高
            Log.e(TAG, "getMatchingSize2: 屏幕密度宽度=" + deviceWidth);
            Log.e(TAG, "getMatchingSize2: 屏幕密度高度=" + deviceHeigh);

            selectSize = sizes[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "getMatchingSize2: 选择的分辨率宽度=" + selectSize.getWidth());
        Log.e(TAG, "getMatchingSize2: 选择的分辨率高度=" + selectSize.getHeight());
        return selectSize;
    }


}