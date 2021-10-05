package com.demo.opengles.record.camera1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.demo.opengles.sdk.EglHelper;
import com.demo.opengles.sdk.EglSurfaceView;
import com.demo.opengles.util.FpsUtil;
import com.demo.opengles.util.TimeConsumeUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

public class VideoRecordEncoder {
    private static final String TAG = "VideoRecordEncoder";

    private Surface mSurface;
    private EGLContext mEGLContext;
    private EglSurfaceView.Renderer mRender;

    private MediaMuxer mMediaMuxer;

    private MediaCodec.BufferInfo mVideoBuffInfo;
    private MediaCodec mVideoEncodec;
    private int width, height;

    private MediaCodec.BufferInfo mAudioBuffInfo;
    private MediaCodec mAudioEncodec;
    private int channel, sampleRate, sampleBit;

    private VideoEncodecThread mVideoEncodecThread;
    private AudioEncodecThread mAudioEncodecThread;
    private EGLMediaThread mEGLMediaThread;

    private boolean encodeStart;
    private boolean audioExit;
    private boolean videoExit;

    private final Object object = new Object();

    private Object tag;

    public boolean isEncodeStart() {
        return encodeStart;
    }

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_WHEN_DIRTY;

    public VideoRecordEncoder(Context context, Object tag) {
        this.tag = tag;
    }

    public void setRender(EglSurfaceView.Renderer wlGLRender) {
        this.mRender = wlGLRender;
    }

    public void setRenderMode(int mRenderMode) {
        if (mRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void requestRender() {
        mEGLMediaThread.requestRender();
    }

    public void initEncoder(EGLContext eglContext, String savePath, int width, int height, int sampleRate, int channel, int sampleBit) {
        this.width = width;
        this.height = height;
        this.sampleRate = sampleRate;
        this.sampleBit = sampleBit;
        this.channel = channel;
        this.mEGLContext = eglContext;
        initMediaEncoder(savePath, width, height, sampleRate, channel);
    }

    private void initMediaEncoder(String savePath, int width, int height, int sampleRate, int channel) {
        try {
            mMediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            // h264
            initVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            // aac
            initAudioEncoder(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channel);

            status = OnStatusChangeListener.STATUS.INIT;
            if (onStatusChangeListener != null) {
                onStatusChangeListener.onStatusChange(status);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void initVideoEncoder(String mineType, int width, int height) {
        try {
            if (width % 2 == 1) {
                width--;
            }
            if (height % 2 == 1) {
                height--;
            }

            Log.e(TAG, "////////////////////////////////////////////////////////////////////////");
            Log.e(TAG, "////////////////////////////////////////////////////////////////////////");
            Log.e(TAG, "////////////////////////////////////////////////////////////////////////");
            Log.e(TAG, "MediaCodec编解码信息如下");
            MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
            for (int i = 0; i < mediaCodecList.getCodecInfos().length; i++) {
                MediaCodecInfo mediaCodecInfo = mediaCodecList.getCodecInfos()[i];

                String supportTypes = "";
                for (int j = 0; j < mediaCodecInfo.getSupportedTypes().length; j++) {
                    supportTypes += mediaCodecInfo.getSupportedTypes()[j] + ";";
                }

                Log.e(TAG, (mediaCodecInfo.isEncoder() ? "编码器" : "解码器")
                        + ", " + (mediaCodecInfo.isSoftwareOnly() ? "软件" : "硬件")
                        + ", " + (mediaCodecInfo.isVendor() ? "厂商" : "安卓")
                        + ", " + mediaCodecInfo.getName() + ", " + supportTypes + ", "
                );
            }
            Log.e(TAG, "////////////////////////////////////////////////////////////////////////");
            Log.e(TAG, "////////////////////////////////////////////////////////////////////////");
            Log.e(TAG, "////////////////////////////////////////////////////////////////////////");

            mVideoEncodec = MediaCodec.createByCodecName("OMX.qcom.video.encoder.avc"); //已知此名称必定为硬件编码器

            MediaCodecInfo usedMediaCodecInfo = mVideoEncodec.getCodecInfo();
            String supportTypes = "";
            for (int j = 0; j < usedMediaCodecInfo.getSupportedTypes().length; j++) {
                supportTypes += usedMediaCodecInfo.getSupportedTypes()[j] + ";";
            }
            Log.e(TAG, (usedMediaCodecInfo.isEncoder() ? "编码器" : "解码器")
                    + ", " + (usedMediaCodecInfo.isSoftwareOnly() ? "软件" : "硬件")
                    + ", " + (usedMediaCodecInfo.isVendor() ? "厂商" : "安卓")
                    + ", " + usedMediaCodecInfo.getName() + ", " + supportTypes + ", "
            );

//            mVideoEncodec = MediaCodec.createEncoderByType(mineType);

            MediaFormat videoFormat = MediaFormat.createVideoFormat(mineType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);//30帧
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);//RGBA
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            //设置压缩等级  默认是baseline
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
//                }
//            }

            mVideoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoBuffInfo = new MediaCodec.BufferInfo();
            mSurface = mVideoEncodec.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
            mVideoEncodec = null;
            mVideoBuffInfo = null;
            mSurface = null;
        }
    }

    private void initAudioEncoder(String mineType, int sampleRate, int channel) {
        try {
            mAudioEncodec = MediaCodec.createByCodecName("OMX.google.aac.encoder"); //已知此名称必定为硬件编码器
//            mAudioEncodec = MediaCodec.createEncoderByType(mineType);
            MediaFormat audioFormat = MediaFormat.createAudioFormat(mineType, sampleRate, channel);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 409600);
            mAudioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mAudioBuffInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
            mAudioEncodec = null;
            mAudioBuffInfo = null;
        }
    }

    public void startRecode() {
        if (mSurface != null && mEGLContext != null) {
            audioPts = 0;
            audioExit = false;
            videoExit = false;
            encodeStart = false;

            mVideoEncodecThread = new VideoEncodecThread(new WeakReference<>(this));
            mAudioEncodecThread = new AudioEncodecThread(new WeakReference<>(this));
            mEGLMediaThread = new EGLMediaThread(new WeakReference<>(this));
            mEGLMediaThread.isCreate = true;
            mEGLMediaThread.isChange = true;
            mEGLMediaThread.start();
            mVideoEncodecThread.start();
            mAudioEncodecThread.start();
        }
    }

    public void stopRecode() {

        if (mVideoEncodecThread != null) {
            mVideoEncodecThread.exit();
            mVideoEncodecThread = null;
        }

        if (mAudioEncodecThread != null) {
            mAudioEncodecThread.exit();
            mAudioEncodecThread = null;
        }

        if (mEGLMediaThread != null) {
            mEGLMediaThread.onDestroy();
            mEGLMediaThread = null;
        }
        audioPts = 0;
        encodeStart = false;
    }

    public void putPcmData(byte[] buffer, int size) {
        if (mAudioEncodecThread != null && !mAudioEncodecThread.isExit && buffer != null && size > 0) {
            int inputBufferIndex = mAudioEncodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer byteBuffer = mAudioEncodec.getInputBuffers()[inputBufferIndex];
                byteBuffer.clear();
                byteBuffer.put(buffer);
                long pts = getAudioPts(size, sampleRate, channel, sampleBit);
//                Log.e("zzz", "AudioTime = " + pts / 1000000.0f);
                mAudioEncodec.queueInputBuffer(inputBufferIndex, 0, size, pts, 0);
            }
        }
    }

    private long audioPts;

    //176400
    private long getAudioPts(int size, int sampleRate, int channel, int sampleBit) {
        audioPts += (long) (1.0 * size / (sampleRate * channel * (sampleBit / 8)) * 1000000.0);
        return audioPts;
    }

    static class VideoEncodecThread extends Thread {
        private WeakReference<VideoRecordEncoder> encoderWeakReference;
        private boolean isExit;

        private int videoTrackIndex;
        private long pts;

        private MediaCodec videoEncodec;
        private MediaCodec.BufferInfo videoBufferinfo;
        private MediaMuxer mediaMuxer;


        public VideoEncodecThread(WeakReference<VideoRecordEncoder> encoderWeakReference) {
            this.encoderWeakReference = encoderWeakReference;

            videoEncodec = encoderWeakReference.get().mVideoEncodec;
            videoBufferinfo = encoderWeakReference.get().mVideoBuffInfo;
            mediaMuxer = encoderWeakReference.get().mMediaMuxer;
            pts = 0;
            videoTrackIndex = -1;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            videoEncodec.start();

            FpsUtil fpsUtil = new FpsUtil("videoEncodec.output");
            while (true) {
                if (isExit) {
                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;
                    encoderWeakReference.get().videoExit = true;

                    if (encoderWeakReference.get().audioExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;

                        status = OnStatusChangeListener.STATUS.END;
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(status);
                        }
                    }

                    break;
                }

                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
//                TimeConsumeUtil.calc("VideoEncodecThread" + encoderWeakReference.get().tag, "videoEncodec读取数据耗时="+outputBufferIndex);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
                    boolean hasAudioEncode = encoderWeakReference.get().mAudioEncodecThread.audioTrackIndex != -1;
//                    boolean hasAudioEncode = true;
                    if (hasAudioEncode) {
                        mediaMuxer.start();
                        encoderWeakReference.get().encodeStart = true;

                        status = OnStatusChangeListener.STATUS.START;
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(status);
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        VideoRecordEncoder videoRecordEncoder = encoderWeakReference.get();
                        if (videoRecordEncoder != null) {
                            TimeConsumeUtil.start("VideoEncodecThread" + videoRecordEncoder.tag);
                        }

                        if (!encoderWeakReference.get().encodeStart) {
//                            SystemClock.sleep(10);
                            continue;
                        }
                        ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferinfo.offset);
                        outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);

                        //设置时间戳
                        if (pts == 0) {
                            pts = videoBufferinfo.presentationTimeUs;
                        }
                        videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;
                        //写入数据
                        mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, videoBufferinfo);
//                        Log.e("zzz", "VideoTime = " + videoBufferinfo.presentationTimeUs / 1000000.0f);
                        if (encoderWeakReference.get().onMediaInfoListener != null) {
                            encoderWeakReference.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }
                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);

                        fpsUtil.trigger();

                        TimeConsumeUtil.calc("VideoEncodecThread" + encoderWeakReference.get().tag, "videoEncodec读取数据耗时@@" + outputBufferIndex);
                    }
                }
            }

        }

        public void exit() {
            isExit = true;
        }
    }

    static class AudioEncodecThread extends Thread {
        private WeakReference<VideoRecordEncoder> encoderWeakReference;
        private boolean isExit;


        private MediaCodec audioEncodec;
        private MediaCodec.BufferInfo audioBufferinfo;
        private MediaMuxer mediaMuxer;

        private int audioTrackIndex;
        private long pts;


        public AudioEncodecThread(WeakReference<VideoRecordEncoder> encoderWeakReference) {
            this.encoderWeakReference = encoderWeakReference;
            audioEncodec = encoderWeakReference.get().mAudioEncodec;
            audioBufferinfo = encoderWeakReference.get().mAudioBuffInfo;
            mediaMuxer = encoderWeakReference.get().mMediaMuxer;
            pts = 0;
            audioTrackIndex = -1;
        }


        @Override
        public void run() {
            super.run();
            isExit = false;
            audioEncodec.start();

            while (true) {
                if (isExit) {
                    audioEncodec.stop();
                    audioEncodec.release();
                    audioEncodec = null;
                    encoderWeakReference.get().audioExit = true;

                    //如果video退出了
                    if (encoderWeakReference.get().videoExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;

                        status = OnStatusChangeListener.STATUS.END;
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(status);
                        }

                    }
                    break;
                }

                int outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    audioTrackIndex = mediaMuxer.addTrack(audioEncodec.getOutputFormat());
                    if (encoderWeakReference.get().mVideoEncodecThread.videoTrackIndex != -1) {
                        mediaMuxer.start();
                        encoderWeakReference.get().encodeStart = true;

                        status = OnStatusChangeListener.STATUS.START;
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(status);
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (!encoderWeakReference.get().encodeStart) {
//                            SystemClock.sleep(10);
                            continue;
                        }

                        ByteBuffer outputBuffer = audioEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(audioBufferinfo.offset);
                        outputBuffer.limit(audioBufferinfo.offset + audioBufferinfo.size);

                        //设置时间戳
                        if (pts == 0) {
                            pts = audioBufferinfo.presentationTimeUs;
                        }
                        audioBufferinfo.presentationTimeUs = audioBufferinfo.presentationTimeUs - pts;
                        //写入数据
                        mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, audioBufferinfo);

                        audioEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
                    }
                }

            }

        }

        public void exit() {
            isExit = true;
        }
    }

    static class EGLMediaThread extends Thread {
        private WeakReference<VideoRecordEncoder> encoderWeakReference;
        private EglHelper eglHelper;
        private Object lock;
        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;


        public EGLMediaThread(WeakReference<VideoRecordEncoder> encoderWeakReference) {
            this.encoderWeakReference = encoderWeakReference;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            lock = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(encoderWeakReference.get().mSurface, encoderWeakReference.get().mEGLContext);
            long drawStartTimeStamp = 0l;

            FpsUtil fpsUtil = new FpsUtil("videoRecord-onDraw" + encoderWeakReference.get().tag);

            while (true) {
                fpsUtil.trigger();
                try {
                    if (isExit) {
                        release();
                        break;
                    }
                    if (isStart) {
                        if (encoderWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                            synchronized (lock) {
                                lock.wait();
                            }
                        } else if (encoderWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                            int fps = 60; //设置视频画面每秒帧数
                            long timePerFame = 1000 / fps;
                            long drawConsume = System.currentTimeMillis() - drawStartTimeStamp;
                            long sleep = timePerFame - drawConsume;
                            if (sleep <= 0) {
                                sleep = timePerFame;
                            }
                            Thread.sleep(sleep);
                        } else {
                            throw new IllegalArgumentException("renderMode");
                        }
                    }

                    onCreate();
                    onChange(encoderWeakReference.get().width, encoderWeakReference.get().height);
                    drawStartTimeStamp = System.currentTimeMillis();

                    onDraw();
                    isStart = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void onCreate() {
            if (!isCreate || encoderWeakReference.get().mRender == null)
                return;

            isCreate = false;
            encoderWeakReference.get().mRender.onSurfaceCreated();
        }

        private void onChange(int width, int height) {
            if (!isChange || encoderWeakReference.get().mRender == null)
                return;

            isChange = false;
            encoderWeakReference.get().mRender.onSurfaceChanged(width, height);
        }

        private void onDraw() {
            if (encoderWeakReference.get().mRender == null)
                return;

            encoderWeakReference.get().mRender.onDrawFrame();
            //第一次的时候手动调用一次 不然不会显示ui
            if (!isStart) {
                encoderWeakReference.get().mRender.onDrawFrame();
            }

            eglHelper.swapBuffers();
        }

        void requestRender() {
            if (lock != null) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }

        void onDestroy() {
            isExit = true;
            //释放锁
            requestRender();
        }


        void release() {
            if (eglHelper != null) {
                eglHelper.destroyEgl();
                eglHelper = null;
                lock = null;
                encoderWeakReference = null;
            }
        }

        EGLContext getEglContext() {
            if (eglHelper != null) {
                return eglHelper.getEglContext();
            }
            return null;
        }
    }

    private OnMediaInfoListener onMediaInfoListener;

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);
    }

    private OnStatusChangeListener onStatusChangeListener;

    public void setOnStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        this.onStatusChangeListener = onStatusChangeListener;
    }

    public static OnStatusChangeListener.STATUS status = OnStatusChangeListener.STATUS.INIT;

    public interface OnStatusChangeListener {
        void onStatusChange(STATUS status);

        enum STATUS {
            INIT,
            START,
            END
        }

    }

}
