package com.demo.opengles.sdk;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public class EglSurfaceView extends SurfaceView {

    private Renderer mRenderer;
    private EGLThread mEGLThread;
    private Surface mSurface;
    private EGLContext mEglContext;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    public EglSurfaceView(Context context) {
        this(context, null);
    }

    public EglSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EglSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(surfaceHolderCallback);
    }

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
//            Log.e("EglSurfaceView", "surfaceCreated, " + Thread.currentThread().getName());
            if (mSurface == null) {
                mSurface = holder.getSurface();
            }
            mEGLThread = new EGLThread(new WeakReference<EglSurfaceView>(EglSurfaceView.this));
            mEGLThread.isCreate = true;
            mEGLThread.start();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//            Log.e("EglSurfaceView", "surfaceChanged, " + Thread.currentThread().getName());
            mEGLThread.width = width;
            mEGLThread.height = height;
            mEGLThread.isChange = true;
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//            Log.e("EglSurfaceView", "surfaceDestroyed, " + Thread.currentThread().getName());
            mEGLThread.onDestroy();
            mEGLThread = null;
            mEglContext = null;
        }
    };

    public void setRenderer(Renderer mRenderer) {
        this.mRenderer = mRenderer;
    }

    public void setRendererMode(int renderMode) {
        if (mRenderer == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = renderMode;
    }

    public void requestRender() {
        if (mEGLThread != null) {
            mEGLThread.requestRender();
        }
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.mSurface = surface;
        this.mEglContext = eglContext;
    }

    public EGLContext getEglContext() {
        if (mEGLThread != null) {
            return mEGLThread.getEglContext();
        }
        return null;
    }

    private static class EGLThread extends Thread {
        private WeakReference<EglSurfaceView> mEGLSurfaceViewWeakRef;
        private EglHelper mEglHelper;

        private int width;
        private int height;

        private boolean isCreate;
        private boolean isChange;
        private boolean isStart;
        private boolean isExit;

        private Object object;

        EGLThread(WeakReference<EglSurfaceView> eGLSurfaceViewWeakRef) {
            this.mEGLSurfaceViewWeakRef = eGLSurfaceViewWeakRef;
        }

        @Override
        public void run() {
            super.run();
            try {
                guardedRun();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void guardedRun() throws InterruptedException {
            isExit = false;
            isStart = false;
            object = new Object();
            mEglHelper = new EglHelper();
            mEglHelper.initEgl(mEGLSurfaceViewWeakRef.get().mSurface, mEGLSurfaceViewWeakRef.get().mEglContext);
            long drawStartTimeStamp = 0l;

            while (true) {
                if (isExit) {
                    // ????????????
                    release();
                    break;
                }

                if (isStart) {
                    if (mEGLSurfaceViewWeakRef.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            object.wait();
                        }
                    } else if (mEGLSurfaceViewWeakRef.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        int fps = 60; //??????????????????????????????
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
                onChange(width, height);
                drawStartTimeStamp = System.currentTimeMillis();
                onDraw();
                isStart = true;
            }
        }

        private void onCreate() {
            if (!isCreate || mEGLSurfaceViewWeakRef.get().mRenderer == null) {
                return;
            }
            isCreate = false;
            mEGLSurfaceViewWeakRef.get().mRenderer.onSurfaceCreated();
        }

        private void onChange(int width, int height) {
            if (!isChange || mEGLSurfaceViewWeakRef.get().mRenderer == null) {
                return;
            }
            isChange = false;
            mEGLSurfaceViewWeakRef.get().mRenderer.onSurfaceChanged(width, height);
        }

        private void onDraw() {
            if (mEGLSurfaceViewWeakRef.get().mRenderer == null) {
                return;
            }
            mEGLSurfaceViewWeakRef.get().mRenderer.onDrawFrame();
            //?????????????????????????????????????????????????????????UI
            if (!isStart) {
                mEGLSurfaceViewWeakRef.get().mRenderer.onDrawFrame();
            }
            mEglHelper.swapBuffers();
        }

        void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        void onDestroy() {
            isExit = true;
            //?????????
            requestRender();
        }

        void release() {
            if (mEglHelper != null) {
                mEglHelper.destroyEgl();
                mEglHelper = null;
                object = null;
                mEGLSurfaceViewWeakRef = null;
            }
        }

        EGLContext getEglContext() {
            if (mEglHelper != null) {
                return mEglHelper.getEglContext();
            }
            return null;
        }
    }

    public interface Renderer {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();
    }
}