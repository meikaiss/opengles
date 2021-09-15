package com.demo.opengles.record;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.util.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PcmRecordActivity extends AppCompatActivity {

    private AudioRecorder audioRecorder;
    private String savePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcm_record);

        findViewById(R.id.btn_start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String dateTime = dateFormat.format(new Date());
                savePath = getExternalCacheDir().getAbsolutePath() + File.separator + dateTime + ".pcm";
                ToastUtil.show("保存路径:" + savePath);

                File saveFile = new File(savePath);
                FileOutputStream fileOutputStream = null;
                try {
                    saveFile.createNewFile();
                    fileOutputStream = new FileOutputStream(saveFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                audioRecorder = new AudioRecorder();
                FileOutputStream finalFileOutputStream = fileOutputStream;
                audioRecorder.setOnAudioDataArrivedListener(new AudioRecorder.OnAudioDataArrivedListener() {
                    @Override
                    public void onAudioDataArrived(byte[] audioData, int length) {
                        try {
                            finalFileOutputStream.write(audioData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                audioRecorder.startRecord();
            }
        });

        findViewById(R.id.btn_stop_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecorder.stopRecord();
            }
        });

        findViewById(R.id.btn_play_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PcmPlayer pcmPlayer = new PcmPlayer();
                pcmPlayer.start(savePath);
            }
        });
    }
}
