package com.demo.opengles.world;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/16.
 */
public class WorldActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;


    World world = new World();
    Cube cube = new Cube();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {


            float downX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    world.inTouch = true;
                    downX = event.getX();

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    float deltaX = event.getX() - downX;

                    world.angleDelta = deltaX / world.getWidth() * 90;


                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

                    world.angle -= world.angleDelta;
                    world.angleDelta = 0;
                    world.inTouch = false;
                }
                return true;
            }
        });

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {


            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                cube.create();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                cube.change(gl, width, height);
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
