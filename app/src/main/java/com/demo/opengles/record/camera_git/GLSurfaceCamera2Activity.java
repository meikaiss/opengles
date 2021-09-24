package com.demo.opengles.record.camera_git;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

public class GLSurfaceCamera2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GLSurfaceCamera2Act";

    private ImageView mCloseIv;
    private ImageView mSwitchCameraIv;
    private ImageView mTakePictureIv;
    private ImageView mPictureIv;
    private Camera2GLSurfaceView mCameraView;

    private Camera2Proxy mCameraProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface_camera2);
        initView();
    }

    private void initView() {
        mCloseIv = findViewById(R.id.toolbar_close_iv);
        mCloseIv.setOnClickListener(this);
        mSwitchCameraIv = findViewById(R.id.toolbar_switch_iv);
        mSwitchCameraIv.setOnClickListener(this);
        mTakePictureIv = findViewById(R.id.take_picture_iv);
        mTakePictureIv.setOnClickListener(this);
        mPictureIv = findViewById(R.id.picture_iv);
        mPictureIv.setOnClickListener(this);
        mCameraView = findViewById(R.id.camera_view);
        mCameraProxy = mCameraView.getCameraProxy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_close_iv:
                finish();
                break;
            case R.id.toolbar_switch_iv:
                mCameraProxy.switchCamera(mCameraView.getWidth(), mCameraView.getHeight());
                mCameraProxy.startPreview();
                break;
            case R.id.take_picture_iv:
                mCameraProxy.setImageAvailableListener(mOnImageAvailableListener);
                mCameraProxy.captureStillPicture(); // 拍照
                break;
            case R.id.picture_iv:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivity(intent);
                break;
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener
            () {
        @Override
        public void onImageAvailable(ImageReader reader) {
            new ImageSaveTask().execute(reader.acquireNextImage()); // 保存图片
        }
    };

    private class ImageSaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... images) {
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mPictureIv.setImageBitmap(bitmap);
        }
    }

}
