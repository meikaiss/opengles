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

    private void createCameraPreviewSession() throws CameraAccessException {
        /**
         * 第3个参数指定此ImageReader提供的图像数据的格式，
         * 值域参考ImageFormat or android.graphics.PixelFormat，
         * 但不是这两个类里面所有格式都支持，例如NV21就不支持。
         */
        mImageReader = ImageReader.newInstance(surfaceView.getWidth(), surfaceView.getHeight(), ImageFormat.JPEG, 3);
        mImageReader.setOnImageAvailableListener(onImageAvailableListener, cameraThreadHandler);

        Surface surface = surfaceView.getHolder().getSurface();

        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surface);
        surfaceList.add(mImageReader.getSurface());

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
            /**
             * 创建一个适用于相机画面预览的捕获请求的建造器
             * 常用参数的意义:
             * TEMPLATE_PREVIEW：创建请求，优先保证高帧率，其次保证图像质量，需配合setRepeatingRequest使用
             * TEMPLATE_STILL_CAPTURE：创建请求，优先保证图像质量，其次保证高帧率，需配合capture使用
             * TEMPLATE_RECORD：创建请求，优先保证稳定的帧率，需配合setRepeatingRequest使用
             * TEMPLATE_VIDEO_SNAPSHOT：创建请求，适合录视频时捕获静止画面，优先保证图像质量，而不考虑正在进行的录制，需配合capture和另一个正在进行的TEMPLATE_RECORD使用
             */
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            /**
             * 注意这个Surface是在创建Session传话时传入的Surface数组中的一个
             */
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


            /**
             * 无限循环的捕获图像，即等同于预览效果，并且尽可能提供最高的帧率。
             * 使用此重复捕获方法，就不需要单独持续执行单次捕获（PS至少提高了java代码效率）
             * 此就去的优先级比Capture更低，即如果在RepeatingRequest时调用Capture，会优先执行Capture
             *
             * 停止重复捕获：stopRepeating。abortCaptures可以清理request，它的停止层级更高
             */
            mSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, cameraThreadHandler);
        } catch (CameraAccessException e) {
            LogUtil.e(e);
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            checkState(result);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            checkState(partialResult);
        }

        private void checkState(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW:
                    //预览画面时，捕获回调里不需要进行额外的处理
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
            //回到自动对焦初始化时的设置的状态，并取消当前激活的对焦触发器
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mState = STATE_PREVIEW;
            //切换回到相机预览状态
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
            //拍照请求构造器，Still静止，StillCapture表示静止图像捕获器，即拍照的意思
            CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());//拍照时，将mImageReader.getSurface()作为目标

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 根据屏幕横竖屏来设置图像的旋转方向，这里为了演示api，不做特殊计算
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
}