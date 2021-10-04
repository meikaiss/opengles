package com.demo.opengles.egl;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.gaussian.render.WaterMarkRenderObject;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.CollectUtil;
import com.demo.opengles.util.OpenGLESUtil;

import java.util.List;

public class EGLCamera1FBOPreviewWaterMarkActivity extends BaseActivity {

    private static final String TAG = "EGLCamera1FBO";

    private Camera camera;
    private EglSurfaceView eglSurfaceView;

    private int cameraTextureId;
    private SurfaceTexture surfaceTexture;

    private CameraRenderObject cameraRenderObject;
    private WaterMarkRenderObject waterMarkRenderObject;
    private DefaultRenderObject defaultRenderObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera1_preview);

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
                cameraTextureId = OpenGLESUtil.getOesTexture();

                cameraRenderObject.onCreate();
                waterMarkRenderObject.onCreate();
                defaultRenderObject.onCreate();

                surfaceTexture = new SurfaceTexture(cameraTextureId);
                surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        Log.e(TAG, "onFrameAvailable, " + Thread.currentThread().getName());
                    }
                });

                try {
                    initCamera1(surfaceTexture);
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
                Log.e(TAG, "onDrawFrame, " + Thread.currentThread().getName());
                surfaceTexture.updateTexImage();
                cameraRenderObject.onDraw(cameraTextureId);
                waterMarkRenderObject.onDraw(cameraRenderObject.fboTextureId);
                defaultRenderObject.onDraw(waterMarkRenderObject.fboTextureId);
            }

        });
        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void initCamera1(SurfaceTexture surfaceTexture) throws Exception {
        Log.e(TAG, "surfaceView.width=" + eglSurfaceView.getWidth() + ", surfaceView.height=" + eglSurfaceView.getHeight());

        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            return;
        }
        int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        camera = Camera.open(cameraId);

        //设置相机参数
        Camera.Parameters parameters = camera.getParameters();
        //系统特性：拍照的聚焦频率要高于拍视频
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
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
        //在使用正交投影变换的情况下，不需要考虑图像宽高比与View宽高比不一致的问题，因为正交投影会保持图像原有的宽高比，允许上下或两侧出现空白
        //所以直接选择最清晰的预览尺寸
        Camera.Size previewSize = sizeList.get(0);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);

        cameraRenderObject.inputWidth = previewSize.width;
        cameraRenderObject.inputHeight = previewSize.height;

        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

}
