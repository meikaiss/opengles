package com.demo.opengles.record;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecorder {

    private static final String TAG = "AudioRecorder";

    public interface OnAudioDataArrivedListener {
        void onAudioDataArrived(byte[] audioData, int length);
    }

    //声源
    private static final int DEFFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    //采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
    private static final int DEFAULT_SAMPLE_RATE_INHZ = 44100;
    //声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    //返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //内部缓冲区大小
    private int minBufferSize = 0;
    //是否已启动录音
    private boolean isStarted = false;
    //是否可以从缓冲区中读取数据
    private boolean canReadDataFromBuffer = true;
    //从缓冲区中读取数据的回调方法
    private OnAudioDataArrivedListener onAudioDataArrivedListener;

    private AudioRecord audioRecord;

    public void setOnAudioDataArrivedListener(OnAudioDataArrivedListener onAudioDataArrivedListener) {
        this.onAudioDataArrivedListener = onAudioDataArrivedListener;
    }

    public boolean startRecord() {
        return startRecord(DEFFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLE_RATE_INHZ,
                DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);
    }

    public boolean startRecord(int audioSource, int sampleRate, int channel, int audioFormat) {
        if (isStarted) {
            Log.e(TAG, "startRecord: AudioRecorder has been already started");
            return false;
        }

        //获取内部缓冲区最小size
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat);
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "startRecord: minBufferSize is error_bad_value");
            return false;
        }
        Log.d(TAG, "startRecord: minBufferSize = " + minBufferSize + "bytes");

        //初始化 audioRecord
        audioRecord = new AudioRecord(audioSource, sampleRate, channel, audioFormat, minBufferSize);
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "startRecord: audioRecord is uninitialized");
            return false;
        }

        //启动录制
        audioRecord.startRecording();

        //可以从内部缓冲区中读取数据
        canReadDataFromBuffer = true;

        //启动子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (canReadDataFromBuffer) {
                    //初始化缓冲区数据接收数组
                    byte[] data = new byte[minBufferSize];

                    //读取内部缓冲区中读取数据
                    int result = audioRecord.read(data, 0, minBufferSize);

                    if (result == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, "run: audioRecord.read result is ERROR_BAD_VALUE");
                    } else if (result == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.e(TAG, "run: audioRecord.read result is ERROR_INVALID_OPERATION");
                    } else {
                        if (onAudioDataArrivedListener != null) {
                            //调用读取数据回调方法
                            onAudioDataArrivedListener.onAudioDataArrived(data, result);
                        }
                        Log.d(TAG, "run: audioRecord read " + result + "bytes");
                    }
                }
            }
        }).start();

        //设置录音已启动
        isStarted = true;
        Log.d(TAG, "startRecord: audioRecorder has been already started");
        return true;
    }

    public void stopRecord() {
        //如果录音尚未启动，直接返回
        if (!isStarted) return;
        //设置内部缓冲区数据不可读取
        canReadDataFromBuffer = false;
        //停止录音
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
        //释放资源
        audioRecord.release();
        //设置录音未启动
        isStarted = false;
        //回调置为空
        onAudioDataArrivedListener = null;
    }
}