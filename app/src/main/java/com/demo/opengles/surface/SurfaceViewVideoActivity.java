package com.demo.opengles.surface;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

import java.io.IOException;

public class SurfaceViewVideoActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view_video);

        surfaceView = findViewById(R.id.surface_view);

        init();
    }

    private void init() {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    playVideo(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width,
                                       int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    private void playVideo(SurfaceHolder surfaceHolder) throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //将此MediaPlayer解码出的图像输出到此SurfaceHolder对应的Surface中，
        // 而这个Surface是由SurfaceView创建的，而SurfaceView在创建时已将此Surface挂载到系统屏幕抽象上，从而能显示到屏幕中
        mediaPlayer.setDisplay(surfaceHolder);
        //设置播放的视频源
        AssetFileDescriptor afd = getAssets().openFd("alpha_video/alpha_video.mp4");
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.prepareAsync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}
