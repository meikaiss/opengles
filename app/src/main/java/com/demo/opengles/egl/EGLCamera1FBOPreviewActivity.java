package com.demo.opengles.egl;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.CollectUtil;

import java.util.List;

public class EGLCamera1FBOPreviewActivity extends AppCompatActivity {

    private static final String TAG = "EGLCamera1FBO";

    private Camera camera;
    private EglSurfaceView eglSurfaceView;

    private int cameraTextureId;
    private SurfaceTexture surfaceTexture;

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
                cameraTextureId = createCameraTexture();

                cameraRenderObject.onCreate();
                defaultRenderObject.onCreate();

                surfaceTexture = new SurfaceTexture(cameraTextureId);

                try {
                    initCamera1(surfaceTexture);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                cameraRenderObject.onChange(width, height);
                defaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame() {
                Log.e(TAG, "onDrawFrame");
                surfaceTexture.updateTexImage();
                cameraRenderObject.onDraw(cameraTextureId);
                defaultRenderObject.onDraw(cameraRenderObject.fboTextureId);
            }

            private int createCameraTexture() {
                int[] texture = new int[1];
                //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
                GLES20.glGenTextures(1, texture, 0);
                //将生成的纹理与gpu关联为外部纹理类型，传入纹理id作为参数，每次bind之后，后续操作的纹理都是该纹理
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
                //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
                //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

                //返回生成的纹理的句柄
                return texture[0];
            }
        });
        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void initCamera1(SurfaceTexture surfaceTexture) throws Exception {
        Log.e(TAG, "surfaceView.width=" + eglSurfaceView.getWidth()
                + ", surfaceView.height=" + eglSurfaceView.getHeight());

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

        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    //根据屏幕的旋转角度、相机的硬件内置放置角度，来设置显示旋转角度
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

}
