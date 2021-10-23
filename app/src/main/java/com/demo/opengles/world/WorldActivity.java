package com.demo.opengles.world;

import static android.opengl.GLES20.GL_TRUE;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/16.
 */
public class WorldActivity extends BaseActivity {

    private static final String TAG = "WorldActivity";

    private GLSurfaceView glSurfaceView;

    private World world = new World();
    private Flat flat = new Flat(activity);
    private Cube cube = new Cube(activity);
    private List<Cube> cubeList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                flat.create();
                cube.create();

                for (int i = 0; i < 4; i++) {
                    Cube cube = new Cube(activity);
                    cube.create();
                    cubeList.add(cube);
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                flat.change(gl, width, height);
                cube.change(gl, width, height);
                for (int i = 0; i < 4; i++) {
                    cubeList.get(i).change(gl, width, height);
                }

                flat.setScale(100, 100,100);
                flat.setTranslate(0, 0, -1);

                //正方体的边长是2，因为横坐标范围从-1到1的长度是2
                cubeList.get(0).setTranslate(4f, 0, 0);
                cubeList.get(1).setTranslate(-4f, 0, 0);
                cubeList.get(2).setTranslate(0, 4f, 0);
                cubeList.get(3).setTranslate(0, -4f, 0);

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                world.draw();
                flat.draw(world.getMVPMatrix());
                cube.draw(world.getMVPMatrix());
                for (int i = 0; i < 4; i++) {
                    cubeList.get(i).draw(world.getMVPMatrix());
                }
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        initTouchListener();
    }

    private class AntiConfigChooser implements GLSurfaceView.EGLConfigChooser {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int attribs[] = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, GL_TRUE,
                    EGL10.EGL_SAMPLES, 4,  // 在这里修改MSAA的倍数，4就是4xMSAA，再往上开程序可能会崩。用来指定采样器的个数，一般来说，移动设备开到 4 基本上是极限了，也有极少数开到 2 或者 8 是极限的
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTouchListener() {
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                /**
                 * 每一次Move所计算出的scale是针对上一次消费掉的Move事件的触摸位置，此方法返回true表示已消费，返回false表示未消费
                 */
                float scaleFactor = detector.getScaleFactor();
                world.onScale(scaleFactor);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        });

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                return world.onTouch(event);
            }
        });
    }

}