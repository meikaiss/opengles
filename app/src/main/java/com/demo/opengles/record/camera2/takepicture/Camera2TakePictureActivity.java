package com.demo.opengles.record.camera2.takepicture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.LogUtil;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * https://zhuanlan.zhihu.com/p/161883764
 * Camera2的AF理解比较全面的文章，吊打微信京东的相机使用
 */
public class Camera2TakePictureActivity extends BaseActivity {

    private static final String TAG = "tag";

    private static final int STATE_PREVIEW = 1;
    private static final int STATE_WAITING_PRE_CAPTURE = 2;
    private int mState = STATE_PREVIEW;

    private SurfaceView surfaceView;
    private Button btnTakePicture;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;
    private CameraDevice cameraDevice;
    private CameraCaptureSession mSession;

    private CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest mPreviewRequest;

    private HandlerThread cameraHandlerThread;
    private Handler cameraThreadHandler;

    private ImageReader mImageReader;
    private File mFile;

    private int cameraId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_take_picture);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        surfaceView = findViewById(R.id.surface_view);

        btnTakePicture = findViewById(R.id.btn_take_picture);

        cameraHandlerThread = new HandlerThread("cameraHandlerThread");
        cameraHandlerThread.start();
        cameraThreadHandler = new Handler(cameraHandlerThread.getLooper());

        mFile = new File(getExternalCacheDir(), "takePicture.jpg");

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    openCamera2();
                } catch (Exception e) {
                    LogUtil.e(e);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockFocus();
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void openCamera2() throws Exception {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        characteristics = cameraManager.getCameraCharacteristics(cameraId + "");

        cameraManager.openCamera(cameraId + "", new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Camera2TakePictureActivity.this.cameraDevice = camera;

                try {
                    createCameraPreviewSession();
                } catch (CameraAccessException e) {
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

    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            LogUtil.e(TAG, "onImageAvailable, " + System.currentTimeMillis());

            Image image = reader.acquireNextImage();

            LogUtil.e(TAG, "image.getFormat() = " + image.getFormat());
            LogUtil.e(TAG, "image.getPlanes().length = " + image.getPlanes().length);

            switch (image.getFormat()) {
                case ImageFormat.JPEG:
                    LogUtil.e(TAG, "ImageReader format is JPEG");
                    break;
                case ImageFormat.YUV_420_888:
                    LogUtil.e(TAG, "ImageReader format is YUV_420_888");
                    break;
                default:
                    break;
            }

            cameraThreadHandler.post(new ImageSaver(image, mFile));
        }
    };

    private void createCameraPreviewSession() throws CameraAccessException {
        mImageReader = ImageReader.newInstance(surfaceView.getWidth(), surfaceView.getHeight(), ImageFormat.JPEG, 3);
        mImageReader.setOnImageAvailableListener(onImageAvailableListener, cameraThreadHandler);

        Surface surface = surfaceView.getHolder().getSurface();

        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surface);
        surfaceList.add(mImageReader.getSurface());

        cameraDevice.createCaptureSession(
                surfaceList,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mSession = cameraCaptureSession;

                        requestPreview();
                    }


                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Toast.makeText(Camera2TakePictureActivity.this, "Camera configuration Failed", Toast.LENGTH_SHORT).show();
                    }
                }, cameraThreadHandler);
    }

    private void requestPreview() {
        try {
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mPreviewBuilder.addTarget(surfaceView.getHolder().getSurface());

            /**
             * 设置相机的自动对焦模式
             *
             * https://developer.android.com/reference/android/hardware/camera2/CaptureRequest?hl=en#CONTROL_AF_MODE
             * AF是auto-focus的缩写，即控制自动对焦的模式
             *
             * OFF
             * 相机专业模式中manual focus会设为这个模式，然后APP下发屈光度，底层转换为相应的马达位置，并将lens推到这个位置。目前只有这一个用途。
             *
             * AUTO
             * 字面意思，自动对焦，但实际上准确的应该叫做单次对焦模式，APP下发一次trigger就对焦一次，APP不发trigger的话lens不会移动。
             *
             * MACRO
             * 与AUTO完全一样，暂时不知道有什么用，也没见APP用到过。
             *
             * CONTINUOUS_VIDEO
             * 连续对焦，这个才是真正的全自动对焦，camera画面有场景变化或camera检测到场景失焦，底层会自动触发对焦，保持camera画面处于合焦状态。
             *
             * CONTINUOUS_PICTURE
             * 与CONTINUOUS_VIDEO一样，对于底层两者没有区别。
             *
             *
             * 重点说明：
             * 1、只有在AF_MODE_AUTO时才需要配AF_REGIONS，CONTINUOUS_VIDEO和CONTINUOUS_PICTURE模式都不需要，写成null，底层会自动选取画面中心的区域；
             * 2、无论API1还是API2，Camera画面坐标的起始位置都是在手机横屏时（导航栏在右）画面的左上角，跟屏和TP的坐标不一样，需要做转换，APP下发touch AE AF的坐标都要遵循这个原则；
             */
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            /**
             * 设置相机的自动暴光模式
             *
             * CONTROL_AE_MODE_ON_AUTO_FLASH
             * 当光线不足时，自动打开暴光
             *
             * 其它值暂不说明
             */
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mPreviewRequest = mPreviewBuilder.build();
            mSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, cameraThreadHandler);
        } catch (CameraAccessException e) {
            LogUtil.e(e);
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            mSession = session;
            checkState(result);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            mSession = session;
            checkState(partialResult);
        }

        private void checkState(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW:
                    // We have nothing to do when the camera preview is working normally.
                    break;
                case STATE_WAITING_PRE_CAPTURE:
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            //mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            //runPrecaptureSequence();//视频拍摄
                        }
                    }
                    break;
            }
        }
    };

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            /**
             * IDLE
             * APP不对trigger做处理的话默认为idle。
             *
             * START
             * Auto mode时AF收到一次CONTROL_AF_TRIGGER_START就开始触发一次对焦，没收到就停在当前位置；
             * Continuous mode时，收到CONTROL_AF_TRIGGER_START就锁定对焦，停在当前位置，底层场景切换自动触发失效。
             *
             * CANCEL
             * 取消当前对焦动作，等待再次触发。
             */
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);

            mState = STATE_WAITING_PRE_CAPTURE;
            // Tell #mCaptureCallback to wait for the lock.
            mSession.capture(mPreviewBuilder.build(), mCaptureCallback,
                    cameraThreadHandler);
        } catch (CameraAccessException e) {
            LogUtil.e(e);
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
     /*       mCaptureSession.capture(mPreviewBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);*/
            // After this, the camera will go back to the normal state of preview.
            mSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, cameraThreadHandler);
        } catch (CameraAccessException e) {
            LogUtil.e(e);
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            if (null == cameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());//拍照时，是将mImageReader.getSurface()作为目标

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);

            CameraCaptureSession.CaptureCallback captureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    ToastUtil.show("保存至: " + mFile);

                    unlockFocus();//恢复预览
                }
            };

            mSession.stopRepeating();
            mSession.abortCaptures();
            mSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            LogUtil.e(e);
        }
    }

}