package com.demo.opengles.record;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.helper.VideoPlayerActivity;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ToastUtil;

/**
 * 4路摄像头同时预览录制
 */
public class EGLCamera1Record4SameTimeActivity extends AppCompatActivity {

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;
    private Button btnCpu;

    private RecordManager recordManager1;
    private RecordManager recordManager2;
    private RecordManager recordManager3;
    private RecordManager recordManager4;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordManager1.onDestroy();
        recordManager2.onDestroy();
        recordManager3.onDestroy();
        recordManager4.onDestroy();
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
        btnCpu = findViewById(R.id.btn_cpu);

        TimeConsumeUtil.calc("findViewById");

        TimeConsumeUtil.start("recordManager1.create");
        recordManager1 = new RecordManager();
        recordManager2 = new RecordManager();
        recordManager3 = new RecordManager();
        recordManager4 = new RecordManager();

        recordManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
        recordManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
        recordManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
        recordManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);

        TimeConsumeUtil.calc("recordManager1.create");

        btnRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TimeConsumeUtil.start("startRecord");
                        recordManager1.startRecord();
                        recordManager2.startRecord();
                        recordManager3.startRecord();
                        recordManager4.startRecord();
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
                        recordManager2.stopRecord();
                        recordManager3.stopRecord();
                        recordManager4.stopRecord();
                        TimeConsumeUtil.calc("stopRecord");
                    }
                }).start();
            }
        });

        btnCpu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 2000; i++) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                float a = 0.1234556789f;
                                float b = 12345.6789f;
                                float c = a * b;

                                Log.e("test", "c = " + c + ", threadName=" + Thread.currentThread().getName());
                            }
                        }
                    });
                    thread.setName("thread_test_" + i);

                    thread.start();
                }
            }
        });

        btnRecordPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordManager1.getSavePath() == null) {
                    ToastUtil.show("请先录制一个视频");
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
