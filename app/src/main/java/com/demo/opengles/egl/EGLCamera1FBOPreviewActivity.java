package com.demo.opengles.egl;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.CollectUtil;
import com.demo.opengles.util.OpenGLESUtil;

import java.util.List;

public class EGLCamera1FBOPreviewActivity extends BaseActivity {

    private static final String TAG = "EGLCamera1FBO";

    private Camera camera;
    private EglSurfaceView eglSurfaceView;

    private int cameraTextureId;
    private SurfaceTexture surfaceTexture;
    private Camera.Size previewSize;

    private CameraRenderObject cameraRenderObject;
    private DefaultRenderObject defaultRenderObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera1_preview);

        cameraRenderObject = new CameraRenderObject(this);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        defaultRenderObject = new DefaultRenderObject(this);
        defaultRenderObject.isBindFbo = false;
        defaultRenderObject.isOES = false;

        eglSurfaceView = findViewById(R.id.egl_surface_view);
        eglSurfaceView.setRenderer(new EglSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated() {
                cameraTextureId = OpenGLESUtil.createOesTexture();

                cameraRenderObject.onCreate();
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

                cameraRenderObject.inputWidth = previewSize.width;
                cameraRenderObject.inputHeight = previewSize.height;
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                cameraRenderObject.onChange(width, height);
                defaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame() {
                Log.e(TAG, "onDrawFrame, " + Thread.currentThread().getName());
                surfaceTexture.updateTexImage();
                cameraRenderObject.onDraw(cameraTextureId);
                defaultRenderObject.onDraw(cameraRenderObject.fboTextureId);
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

        //?????????opengGL??????????????????????????????setDisplayOrientation?????????????????????????????????
//        int degree = computeDegrees(cameraId);
//        Log.e(TAG, "????????????????????????????????????????????????????????? = " + degree);
//        camera.setDisplayOrientation(degree);

        //??????????????????
        Camera.Parameters parameters = camera.getParameters();
        //??????????????????????????????????????????????????????
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
        //???????????????????????????????????????????????????????????????????????????View?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //??????????????????????????????????????????
        previewSize = sizeList.get(0);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);

        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    //?????????????????????????????????????????????????????????????????????????????????????????????
    private int computeDegrees(int cameraId) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
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
            //????????????????????????????????????
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //????????????
            result = (cameraInfo.orientation - degrees) % 360;
        } else {
            //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            result = (cameraInfo.orientation - degrees) % 360;
        }

        return result;
    }

}
