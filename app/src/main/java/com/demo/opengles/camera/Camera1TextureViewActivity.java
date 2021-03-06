package com.demo.opengles.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.CollectUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Camera1TextureViewActivity extends BaseActivity {
    private static final String TAG = "Camera1TV_Act";

    private TextureView textureView;
    private Camera camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_1_textureview);

        textureView = findViewById(R.id.texture_view);

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
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width,
                                                  int height) {
                try {
                    startCamera1(surface);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width,
                                                    int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
    }

    @SuppressWarnings("deprecation")
    private void startCamera1(SurfaceTexture surface) throws IOException {
        Log.e(TAG, "surfaceView.width=" + textureView.getWidth()
                + ", surfaceView.height=" + textureView.getHeight());

        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            return;
        }
        int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        camera = Camera.open(cameraId);

        int degree = computeDegrees(cameraId);
        Log.e(TAG, "????????????????????????????????????????????????????????? = " + degree);
        camera.setDisplayOrientation(degree);

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

        Camera.Size previewSize = adjustSurfaceViewWidthHeight(sizeList);
        previewSize = Collections.max(sizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return ((o1.width + o1.height) >= (o2.width + o2.height)) ? 1 : -1;
            }
        });
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        Camera.Size pictureSize = pictureSizeList.get(0);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        camera.setParameters(parameters);

        camera.setParameters(parameters);
        camera.setPreviewTexture(surface);

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

    /**
     * ???????????????????????????????????????????????????????????????????????????View??????????????????????????????????????????
     * ???????????????????????????????????????????????????View???????????????????????????????????????View?????????????????????????????????????????????
     * <p>
     * ???????????????View??????????????????????????????
     * 1??????????????????View???????????????????????????????????????????????????View??????????????????????????????????????????
     */
    private Camera.Size adjustSurfaceViewWidthHeight(List<Camera.Size> sizeList) {
        float ASPECT_TOLERANCE = 0.1f;

        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);

            int cameraWidth = Math.min(size.width, size.height);
            int cameraHeight = Math.max(size.width, size.height);
            float cameraSizeScale = (float) cameraWidth / cameraHeight;
            float surfaceViewSizeScale = (float) textureView.getWidth() / textureView.getHeight();

            if (Math.abs(cameraSizeScale - surfaceViewSizeScale) < ASPECT_TOLERANCE) {
                Log.e(TAG, "???????????????????????????????????????????????????size =" + size.width + ", " + size.height);
                return size;
            }
        }

        int minHeightDiff = Integer.MAX_VALUE;
        Camera.Size targetSize = null;
        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);

            int cameraWidth = Math.min(size.width, size.height);
            int cameraHeight = Math.max(size.width, size.height);

            int diff = Math.abs(textureView.getHeight() - cameraHeight);
            if (diff < minHeightDiff) {
                targetSize = size;
                minHeightDiff = diff;
            }
        }

        Log.e(TAG, "???????????????????????????size =" + targetSize.width + ", " + targetSize.height);
        return targetSize;
    }
}
