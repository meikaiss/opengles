package com.demo.opengles.record.camera2.record;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.helper.VideoPlayerActivity;
import com.demo.opengles.record.camera1.VideoRecordEncoder;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ToastUtil;

public class EGLCamera2GLSurfaceView4RecordActivity extends AppCompatActivity {

    private Camera2EGLSurfaceViewRecordManager recordManager1;
    private Camera2EGLSurfaceViewRecordManager recordManager2;
    private Camera2EGLSurfaceViewRecordManager recordManager3;
    private Camera2EGLSurfaceViewRecordManager recordManager4;

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_glsurfaceview_record4);

        btnRecordStart = findViewById(R.id.btn_start_record);
        btnRecordStop = findViewById(R.id.btn_stop_record);
        btnRecordPlay = findViewById(R.id.btn_play_record);

        recordManager1 = new Camera2EGLSurfaceViewRecordManager();
        if (has4Camera()) {
            recordManager2 = new Camera2EGLSurfaceViewRecordManager();
            recordManager3 = new Camera2EGLSurfaceViewRecordManager();
            recordManager4 = new Camera2EGLSurfaceViewRecordManager();
        }

        recordManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
        if (has4Camera()) {
            recordManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
            recordManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
            recordManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);
        }

        initBtnClickListener();
    }

    private boolean has4Camera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            return cameraManager.getCameraIdList().length >= 4;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recordManager1.onDestroy();
        if (has4Camera()) {
            recordManager2.onDestroy();
            recordManager3.onDestroy();
            recordManager4.onDestroy();
        }
    }

    private void initBtnClickListener() {
        btnRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TimeConsumeUtil.start("startRecord");
                        recordManager1.startRecord();
                        if (has4Camera()) {
                            recordManager2.startRecord();
                            recordManager3.startRecord();
                            recordManager4.startRecord();
                        }
                        TimeConsumeUtil.calc("startRecord");
                    }
                }).start();
            }
        });
        btnRecordStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TimeConsumeUtil.start("stopRecord");
                        recordManager1.stopRecord();
                        if (has4Camera()) {
                            recordManager2.stopRecord();
                            recordManager3.stopRecord();
                            recordManager4.stopRecord();
                        }
                        TimeConsumeUtil.calc("stopRecord");
                    }
                }).start();
            }
        });

        btnRecordPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordManager1.getSavePath() == null) {
                    ToastUtil.show("请先完成录制");
                    return;
                }
                if (VideoRecordEncoder.status == VideoRecordEncoder.OnStatusChangeListener.STATUS.START) {
                    ToastUtil.show("请先结束录制");
                    return;
                }

                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("path", recordManager1.getSavePath());
                startActivity(intent);
            }
        });
    }

}
