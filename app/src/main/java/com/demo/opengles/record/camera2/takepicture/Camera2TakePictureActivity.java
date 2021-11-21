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
import java.util.Arrays;

public class Camera2TakePictureActivity extends BaseActivity {

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
            LogUtil.e("onImageAvailable, " + System.currentTimeMillis());

            Image image = reader.acquireNextImage();

            LogUtil.e("image.getFormat() = " + image.getFormat());

            switch (image.getFormat()) {
                case ImageFormat.JPEG:
                    LogUtil.e("ImageReader format is JPEG");
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

        cameraDevice.createCaptureSession(
                Arrays.asList(surface, mImageReader.getSurface()),
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
        Surface surface = surfaceView.getHolder().getSurface();

        try {
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mPreviewBuilder.addTarget(surface);
            //mPreviewBuilder.addTarget(mImageReader.getSurface());

            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
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
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);

            mState = STATE_WAITING_PRE_CAPTURE;
            // Tell #mCaptureCallback to wait for the lock.
            mSession.capture(mPreviewBuilder.build(), mCaptureCallback,
                    cameraThreadHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

}