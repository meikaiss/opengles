package com.demo.opengles.record.camera2.glrecordconcat;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.helper.VideoPlayerActivity;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ToastUtil;

public class Camera2GLSurfaceViewConcatRecordActivity extends BaseActivity {

    private Camera2GLSurfaceViewConcatRecordManager recordManager;

    private GLSurfaceView glSurfaceView;

    private TextView tvStatus;
    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    private AutoTestLog autoTestLog = new AutoTestLog();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_glsurfaceview_record_concat);
        autoTestLog.createFile(this);

        autoTestLog.writeAppend("onCreate");

        glSurfaceView = findViewById(R.id.egl_surface_view_1);

        recordManager = new Camera2GLSurfaceViewConcatRecordManager();
        recordManager.create(this, glSurfaceView, new int[]{0, 1, 2, 3});

        initBtnClickListener();

        tvStatus = findViewById(R.id.tv_status);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isDestroyed()) {
                        return;
                    }
                    SystemClock.sleep(1000);
                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            if (recordManager.isStart()) {
                                tvStatus.setText("ON");
                            } else {
                                tvStatus.setText("OFF");
                            }
                        }
                    });
                }
            }
        }).start();

        autoTestLog.writeAppend("5秒后启动自动化测试");
        autoTestHandler.sendEmptyMessageDelayed(1, 5_000);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                autoTestLog.writeAppend("发生未捕获的异常:" + e.getMessage());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoTestLog.writeAppend("onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoTestLog.writeAppend("onDestroy");

        for (int i = 0; i < 20; i++) {
            autoTestHandler.removeMessages(i);
        }
    }

    private Handler autoTestHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //启动DVR自动化测试，5秒后开始自动循环录制
                    autoTestLog.writeAppend("启动DVR自动化测试，5秒后开始自动循环录制");
                    autoTestHandler.sendEmptyMessageDelayed(2, 5_000);
                    break;
                case 2:
                    autoTestLog.writeAppend("点击开始录制，30秒后点击停止录制");
                    btnRecordStart.callOnClick();
                    autoTestHandler.sendEmptyMessageDelayed(3, 30_000);
                    break;
                case 3:
                    autoTestLog.writeAppend("点击停止录制，5秒后开始整理缓存，最多保留%d个录制MP4文件", autoTestLog.maxMp4CacheCount);
                    btnRecordStop.callOnClick();
                    autoTestHandler.sendEmptyMessageDelayed(4, 5_000);
                    break;
                case 4:
                    autoTestLog.writeAppend("开始整理缓存...");
                    autoTestLog.manageCache();
                    autoTestLog.writeAppend("整理完毕...5秒后开始下一次录制");
                    autoTestHandler.sendEmptyMessageDelayed(2, 5_000);
                    break;
            }

        }
    };

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
