package com.demo.opengles.record.camera2.glrecorddouble;

import android.content.Intent;
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
 * 一个相机输出图像到两个SurfacView，并对两个SurfaceView进行录制
 */
public class Camera2GLSurfaceViewDoubleRecordActivity extends BaseActivity {

    private Camera2GLSurfaceViewDoubleRecordManager recordManager;

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_glsurfaceview_record_double);

        recordManager = new Camera2GLSurfaceViewDoubleRecordManager();

        recordManager.create(this, findViewById(R.id.egl_surface_view_1), findViewById(R.id.egl_surface_view_2), 0);

        initBtnClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recordManager.onDestroy();
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
                String path = recordManager.getSavePath1();
                if (path == null) {
                    path = recordManager.getSavePath2();
                }
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
