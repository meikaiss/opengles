package com.demo.opengles.light;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.egl.AntiConfigChooser;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.world.common.Volume;
import com.demo.opengles.world.game.World;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LightActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;

    private World world = new World();
    private Volume cube = new Volume(activity);

    float[] ambient = {0.9f, 0.9f, 0.9f, 1.0f,};
    float[] diffuse = {0.5f, 0.5f, 0.5f, 1.0f,};
    float[] specular = {1.0f, 1.0f, 1.0f, 1.0f,};
    float[] lightPosition = {10.5f, 10.5f, 10.5f, 0.0f,};

    float[] materialAmb = {0.0f, 0.0f, 1.0f, 1.0f};
    float[] materialDiff = {0.0f, 0.0f, 1.0f, 1.0f};//漫反射设置蓝色
    float[] materialSpec = {1.0f, 0.0f, 0.0f, 1.0f};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        glSurfaceView = findViewById(R.id.surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                cube.create();

                world.eyeXYZ(-30, -30, 10);
                world.directionXYZ(10, 10, 0);

                //启用光照功能
                gl.glEnable(GL10.GL_LIGHTING);
                //开启0号光源
                gl.glEnable(GL10.GL_LIGHT0);


                //通过 glLightfv 函数来指定各种反射光的颜色
                //设置环境光颜色
                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, OpenGLESUtil.createFloatBuffer(ambient));
                //设置漫反射光颜色
                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, OpenGLESUtil.createFloatBuffer(diffuse));
                //设置镜面反射光颜色
                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, OpenGLESUtil.createFloatBuffer(specular));
                //设置光源位置
                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, OpenGLESUtil.createFloatBuffer(lightPosition));


                //设置材料的属性
                //材料对环境光的反射情况
                gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, OpenGLESUtil.createFloatBuffer(materialAmb));
                //散射光的反射情况
                gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, OpenGLESUtil.createFloatBuffer(materialDiff));
                //镜面光的反射情况
                gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, OpenGLESUtil.createFloatBuffer(materialSpec));
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                cube.change(gl, width, height);
                cube.setTranslate(0, 0, 0);
                cube.setScale(5, 5, 5);
            }

            @Override
            public void onDrawFrame(GL10 gl) {

                world.draw();

                cube.draw(world.getMVPMatrix());
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}
