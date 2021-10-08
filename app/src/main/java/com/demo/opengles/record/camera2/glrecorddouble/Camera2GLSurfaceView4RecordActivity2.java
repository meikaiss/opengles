package com.demo.opengles.record.camera2.glrecorddouble;

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

public class Camera2GLSurfaceView4RecordActivity2 extends BaseActivity {

    private Camera2GLSurfaceViewRecordManager2 recordManager1;
    private Camera2GLSurfaceViewRecordManager2 recordManager2;
    private Camera2GLSurfaceViewRecordManager2 recordManager3;
    private Camera2GLSurfaceViewRecordManager2 recordManager4;

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    //是否启用四路摄像头，方便快速切换验证效果
    private boolean enable4Camera() {
        boolean enable4 = false;
        return enable4 && has4Camera();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_glsurfaceview_record4);

        recordManager1 = new Camera2GLSurfaceViewRecordManager2();
        if (enable4Camera()) {
            recordManager2 = new Camera2GLSurfaceViewRecordManager2();
            recordManager3 = new Camera2GLSurfaceViewRecordManager2();
            recordManager4 = new Camera2GLSurfaceViewRecordManager2();
        }

        recordManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
        if (enable4Camera()) {
            recordManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
            recordManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
            recordManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);
        }

        initBtnClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recordManager1.onDestroy();
        if (enable4Camera()) {
            recordManager2.onDestroy();
            recordManager3.onDestroy();
            recordManager4.onDestroy();
        }
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

    private void initBtnClickListener() {

        btnRecordStart = findViewById(R.id.btn_start_record);
        btnRecordStop = findViewById(R.id.btn_stop_record);
        btnRecordPlay = findViewById(R.id.btn_play_record);

        btnRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TimeConsumeUtil.start("startRecord");
                        recordManager1.startRecord();
                        if (enable4Camera()) {
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
                        if (enable4Camera()) {
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
                String path = recordManager1.getSavePath1();
                if (path == null) {
                    path = recordManager1.getSavePath2();
                }
                if (path == null) {
                    ToastUtil.show("请先完成录制");
                    return;
                }
                if (recordManager1.isStart()) {
                    ToastUtil.show("请先结束录制");
                    return;
                }

                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);
            }
        });
    }

}
