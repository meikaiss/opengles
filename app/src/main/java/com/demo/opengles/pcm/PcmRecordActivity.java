package com.demo.opengles.pcm;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PcmRecordActivity extends AppCompatActivity {

    private PcmRecorder audioRecorder;
    private String savePath;

    private PcmEncoder pcmEncoder;
    private String savePathEncoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcm_record);

        initPCM();
        initPCMEncode();
    }

    private void initPCM() {
        findViewById(R.id.btn_start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String dateTime = dateFormat.format(new Date());
                savePath = getExternalCacheDir().getAbsolutePath() + File.separator + dateTime + ".pcm";
                ToastUtil.show("保存路径:" + savePath);

                audioRecorder = new PcmRecorder();
                audioRecorder.start(savePath);

            }
        });

        findViewById(R.id.btn_stop_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecorder.stop();
            }
        });

        findViewById(R.id.btn_play_pcm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PcmPlayer pcmPlayer = new PcmPlayer();
                pcmPlayer.start(savePath);
            }
        });
    }

    private void initPCMEncode() {
        findViewById(R.id.btn_start_record_encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String dateTime = dateFormat.format(new Date());
                savePathEncoder = getExternalCacheDir().getAbsolutePath() + File.separator + dateTime + ".aac";
                ToastUtil.show("保存路径:" + savePathEncoder);

                pcmEncoder = new PcmEncoder();
                pcmEncoder.start(savePathEncoder);

            }
        });

        findViewById(R.id.btn_stop_record_encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pcmEncoder.stop();
            }
        });

        findViewById(R.id.btn_play_pcm_encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PcmDecoder pcmDecoder = new PcmDecoder();
                pcmDecoder.start(savePathEncoder);
            }
        });
    }
}
