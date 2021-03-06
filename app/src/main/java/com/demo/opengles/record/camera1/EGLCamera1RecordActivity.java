package com.demo.opengles.record.camera1;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.gaussian.render.WaterMarkRenderObject;
import com.demo.opengles.helper.VideoPlayerActivity;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.CollectUtil;
import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EGLCamera1RecordActivity extends BaseActivity {

    private Camera camera;
    private EglSurfaceView eglSurfaceView;

    private int cameraTextureId;
    private SurfaceTexture cameraSurfaceTexture;

    private CameraRenderObject cameraRenderObject;
    private WaterMarkRenderObject waterMarkRenderObject;
    private DefaultRenderObject defaultRenderObject;

    private VideoRecordEncoder videoEncodeRecode;
    private AudioRecorder audioRecorder;

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    private String savePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera1_record);

        btnRecordStart = findViewById(R.id.btn_start_record);
        btnRecordStop = findViewById(R.id.btn_stop_record);
        btnRecordPlay = findViewById(R.id.btn_play_record);

        cameraRenderObject = new CameraRenderObject(this);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        waterMarkRenderObject = new WaterMarkRenderObject(this);
        waterMarkRenderObject.isBindFbo = true;
        waterMarkRenderObject.isOES = false;
        defaultRenderObject = new DefaultRenderObject(this);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;

        eglSurfaceView = findViewById(R.id.egl_surface_view);
        eglSurfaceView.setRenderer(new EglSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated() {
                cameraTextureId = OpenGLESUtil.createOesTexture();

                cameraRenderObject.onCreate();
                waterMarkRenderObject.onCreate();
                defaultRenderObject.onCreate();

                cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);
                cameraSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    }
                });

                try {
                    initCamera1(cameraSurfaceTexture);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                cameraRenderObject.onChange(width, height);
                waterMarkRenderObject.onChange(width, height);
                defaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame() {
                cameraSurfaceTexture.updateTexImage();
                cameraRenderObject.onDraw(cameraTextureId);
                waterMarkRenderObject.onDraw(cameraRenderObject.fboTextureId);
                defaultRenderObject.onDraw(waterMarkRenderObject.fboTextureId);
            }

        });
        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_CONTINUOUSLY);

        btnRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });
        btnRecordStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });
        btnRecordPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (savePath == null) {
                    ToastUtil.show("????????????????????????");
                    return;
                }

                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("path", savePath);
                startActivity(intent);
            }
        });
    }

    private void startRecord() {
        videoEncodeRecode = new VideoRecordEncoder(this, null);
        videoEncodeRecode.setRender(new EglSurfaceView.Renderer() {

            DefaultRenderObject defaultRenderObject;

            @Override
            public void onSurfaceCreated() {
                defaultRenderObject = new DefaultRenderObject(EGLCamera1RecordActivity.this);
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
        videoEncodeRecode.setRenderMode(VideoRecordEncoder.RENDERMODE_CONTINUOUSLY);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        savePath = getExternalCacheDir().getAbsolutePath() + File.separator + dateTime + ".mp4";
        ToastUtil.show("????????????:" + savePath);

        videoEncodeRecode.initEncoder(eglSurfaceView.getEglContext(), savePath,
                eglSurfaceView.getWidth(), eglSurfaceView.getHeight(), 44100, 2, 16);
        videoEncodeRecode.startRecode();

        audioRecorder = new AudioRecorder();
        audioRecorder.setOnAudioDataArrivedListener(new AudioRecorder.OnAudioDataArrivedListener() {
            @Override
            public void onAudioDataArrived(byte[] audioData, int length) {
                videoEncodeRecode.putPcmData(audioData, length);
            }
        });
        audioRecorder.startRecord();
    }

    private void stopRecord() {
        audioRecorder.stopRecord();
        videoEncodeRecode.stopRecode();
        videoEncodeRecode = null;
        ToastUtil.show("????????????");
    }

    private void initCamera1(SurfaceTexture surfaceTexture) throws Exception {

        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            return;
        }
        int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        camera = Camera.open(cameraId);

        //??????????????????
        Camera.Parameters parameters = camera.getParameters();
        //??????????????????????????????????????????????????????
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        parameters.setPictureFormat(ImageFormat.JPEG);
//        parameters.setJpegQuality(100);
        CollectUtil.execute(parameters.getSupportedPreviewFormats(),
                new CollectUtil.Executor<Integer>() {
                    @Override
                    public void execute(Integer integer) {
                    }
                });

        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        CollectUtil.execute(sizeList, new CollectUtil.Executor<Camera.Size>() {
            @Override
            public void execute(Camera.Size size) {
            }
        });
        //???????????????????????????????????????????????????????????????????????????View?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //??????????????????????????????????????????
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

}
