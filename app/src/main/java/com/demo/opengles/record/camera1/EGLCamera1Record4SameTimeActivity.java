package com.demo.opengles.record.camera1;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.helper.VideoPlayerActivity;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ToastUtil;

/**
 * 4路摄像头同时预览录制
 */
public class EGLCamera1Record4SameTimeActivity extends BaseActivity {

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    private Camera1RecordManager recordManager1;
    private Camera1RecordManager recordManager2;
    private Camera1RecordManager recordManager3;
    private Camera1RecordManager recordManager4;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TimeConsumeUtil.start("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera1_record4_same_time);
        TimeConsumeUtil.calc("onCreate");

        TimeConsumeUtil.start("findViewById");
        btnRecordStart = findViewById(R.id.btn_start_record);
        btnRecordStop = findViewById(R.id.btn_stop_record);
        btnRecordPlay = findViewById(R.id.btn_play_record);

        TimeConsumeUtil.calc("findViewById");

        TimeConsumeUtil.start("recordManager1.create");
        recordManager1 = new Camera1RecordManager();
        if (has4Camera()) {
            recordManager2 = new Camera1RecordManager();
            recordManager3 = new Camera1RecordManager();
            recordManager4 = new Camera1RecordManager();
        }

        recordManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
        if (has4Camera()) {
            recordManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
            recordManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
            recordManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);
        }

        TimeConsumeUtil.calc("recordManager1.create");

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
                    ToastUtil.show("请先录制一个视频");
                    return;
                }

                if (recordManager1.isStart()) {
                    ToastUtil.show("请先结束录制");
                    return;
                }

                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("path", recordManager1.getSavePath());
                startActivity(intent);
            }
        });
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


}
