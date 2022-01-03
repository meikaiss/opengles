package com.demo.opengles.cubemap;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.world.game.Axis;
import com.demo.opengles.world.game.World;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CubeMapActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;


    private World world = new World();
    private Axis axis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube_map);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                axis = new Axis(activity);
                axis.create();

                world.eyeXYZ(-20, -20, 20);
                world.setInitDirectionXYZ(20, 20, -20);

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                axis.change(gl, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                world.draw();
                axis.draw(world.getMVPMatrix());
            }
        });


        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

}
