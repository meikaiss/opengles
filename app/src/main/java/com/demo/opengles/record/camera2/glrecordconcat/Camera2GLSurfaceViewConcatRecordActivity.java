package com.demo.opengles.record.camera2.glrecordconcat;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.helper.VideoPlayerActivity;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ToastUtil;

public class Camera2GLSurfaceViewConcatRecordActivity extends BaseActivity {

    private Camera2GLSurfaceViewConcatRecordManager recordManager;

    private GLSurfaceView glSurfaceView;

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_glsurfaceview_record_concat);

        glSurfaceView = findViewById(R.id.egl_surface_view_1);

        recordManager = new Camera2GLSurfaceViewConcatRecordManager();
        recordManager.create(this, glSurfaceView, new int[]{0, 1, 2, 3});

        initBtnClickListener();
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
                        recordManager.startRecord();
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
                        recordManager.stopRecord();
                        TimeConsumeUtil.calc("stopRecord");
                    }
                }).start();
            }
        });

        btnRecordPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = recordManager.getSavePath();
                if (path == null) {
                    ToastUtil.show("请先完成录制");
                    return;
                }
                if (recordManager.isStart()) {
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
