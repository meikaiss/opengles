package com.demo.opengles.mediarecorder.camera2.background;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.helper.VideoPlayerActivity;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.FormatUtil;
import com.demo.opengles.util.LogUtil;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Camera2BackgroundRecordActivity extends BaseActivity {

    private Button btnStart;
    private Button btnStop;
    private Button btnPlay;


    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private CameraDevice cameraDevice;
    private CameraCaptureSession mSession;

    private CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest mPreviewRequest;

    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;

    private MediaRecorder mediaRecorder;

    private int cameraId = 0;
    private File file;
    private boolean recordFlag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_record_background);

        btnStart = findViewById(R.id.btn_start_record);
        btnStop = findViewById(R.id.btn_stop_record);
        btnPlay = findViewById(R.id.btn_play_record);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        cameraHandlerThread = new HandlerThread("cameraHandlerThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                initMediaRecorder();

                try {
                    openCamera2();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                recordFlag = true;
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mediaRecorder.stop();
                        mSession.close();
                        cameraDevice.close();
                    }
                });

                recordFlag = false;
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file == null || !file.exists() || file.length() == 0) {
                    ToastUtil.show("请先录制一个视频");
                    return;
                }
                if (recordFlag) {
                    //不够健壮，但此程序仅用于验证后台录制的可行性，暂不考虑这些细节
                    ToastUtil.show("请先结束录制");
                    return;
                }

                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("path", file.getAbsolutePath());
                startActivity(intent);
            }
        });
    }

    private void initMediaRecorder() {
        String time = FormatUtil.get_yyyy_MM_DD_HH_mm_ss();
        file = new File(getExternalCacheDir(), "camera_" + cameraId + "_" + time + ".mp4");
        if (file.exists()) {
            file.delete();
        }

        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId + "");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);


        Size mPreviewSize = sizes[0];
        if (sizes.length >= 23) {
            //仅仅是为了在vivo iQOO手机上运行时，能观察到效果不错的录制视频，第22项为1280x960，其尺寸等同于长安的摄像头
            mPreviewSize = sizes[22];
        }

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder = null;
        }

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频来源
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//设置视频来源
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//设置输出格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择AAC
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);//设置视频编码格式，请注意这里使用默认，实际app项目需要考虑兼容问题，应该选择H264
        mediaRecorder.setVideoEncodingBitRate(8 * mPreviewSize.getWidth() * mPreviewSize.getHeight());//设置比特率 一般是 1*分辨率 到 10*分辨率 之间波动。比特率越大视频越清晰但是视频文件也越大。
        mediaRecorder.setVideoFrameRate(30);//设置帧数， 过大帧数也会让视频文件更大当然也会更流畅，但是没有多少实际提升。人眼极限也就30帧了。
        mediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mediaRecorder.setOrientationHint(90);//旋转90度后，生成的视频宽为960高为1280，相机硬件方向与手机屏幕方向按行业惯例成90度
        //mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        mediaRecorder.setOutputFile(file.getAbsolutePath());


        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera2() throws Exception {
        characteristics = cameraManager.getCameraCharacteristics(cameraId + "");

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2BackgroundRecordActivity.this.cameraDevice = camera;

                try {
                    createCameraSession();
                } catch (Exception e) {
                    LogUtil.e(e);
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
        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(mediaRecorder.getSurface());

        /**
         * Capture捕获，Session会话
         * 创建CaptureSession时传入的Surface列表，表示后续对此Session的所有Capture操作，只能以此Surface列表中的对象作为目标。
         */
        cameraDevice.createCaptureSession(
                surfaceList,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mSession = cameraCaptureSession;

                        requestCapture();

                        mediaRecorder.start();
                    }


                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Toast.makeText(Camera2BackgroundRecordActivity.this, "Camera configuration Failed", Toast.LENGTH_SHORT).show();
                    }
                }, cameraThreadHandler);
    }

    private void requestCapture() {
        try {
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreviewBuilder.addTarget(mediaRecorder.getSurface());
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            mPreviewRequest = mPreviewBuilder.build();

            mSession.setRepeatingRequest(mPreviewRequest, null, cameraThreadHandler);
        } catch (CameraAccessException e) {
            LogUtil.e(e);
        }
    }

}
