package com.demo.opengles.earth;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

public class EarthActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;
    private EarthRenderer renderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earth);

        glSurfaceView = findViewById(R.id.gl_surface_view);

        glSurfaceView.setEGLContextClientVersion(3);
        renderer = new EarthRenderer(this);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_x_d) {
            renderer.eyeX -= 0.1f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_x_a) {
            renderer.eyeX += 0.1f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_y_d) {
            renderer.eyeY -= 0.1f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_y_a) {
            renderer.eyeY += 0.1f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_z_d) {
            renderer.eyeZ -= 0.1f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_z_a) {
            renderer.eyeZ += 0.1f;
            glSurfaceView.requestRender();
        }
        /////////////////////////////////////////////////////////////////////////////////
        if (view.getId() == R.id.btn_rx_d) {
            renderer.angleX -= 5f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_rx_a) {
            renderer.angleX += 5f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_ry_d) {
            renderer.angleY -= 5f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_ry_a) {
            renderer.angleY += 5f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_rz_d) {
            renderer.angleZ -= 5f;
            glSurfaceView.requestRender();
        } else if (view.getId() == R.id.btn_rz_a) {
            renderer.angleZ += 5f;
            glSurfaceView.requestRender();
        }

    }

}