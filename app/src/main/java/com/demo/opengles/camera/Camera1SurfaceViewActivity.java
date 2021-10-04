package com.demo.opengles.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.CollectUtil;

import java.io.IOException;
import java.util.List;

public class Camera1SurfaceViewActivity extends BaseActivity {
    private static final String TAG = "Camera1SV_Act";

    private SurfaceView surfaceView;
    private Camera camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_1_surfaceview);

        surfaceView = findViewById(R.id.surface_view);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 1000);
        }

        findViewById(R.id.img_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.tv_info).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.tv_info).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.tv_info).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
        }
    }

    private void init() {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    startCamera1(holder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width,
                                       int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    @SuppressWarnings("deprecation")
    private void startCamera1(SurfaceHolder holder) throws IOException {
        Log.e(TAG, "surfaceView.width=" + surfaceView.getWidth()
                + ", surfaceView.height=" + surfaceView.getHeight());

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
        Camera.Size size = adjustSurfaceViewWidthHeight(sizeList);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);

        camera.setPreviewDisplay(holder);
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
