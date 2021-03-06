package com.demo.opengles.main;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.demo.opengles.camera.Camera1GLSurfaceViewActivity;
import com.demo.opengles.camera.Camera1GLSurfaceViewTakePhotoActivity;
import com.demo.opengles.camera.Camera1SurfaceViewActivity;
import com.demo.opengles.camera.Camera1TakePhotoGLSurfaceViewActivity;
import com.demo.opengles.camera.Camera1TextureViewActivity;
import com.demo.opengles.cubemap.CubeMapActivity;
import com.demo.opengles.databinding.MainActivityBinding;
import com.demo.opengles.earth.EarthActivity;
import com.demo.opengles.egl.EGLActivity;
import com.demo.opengles.egl.EGLCamera1FBOPreviewActivity;
import com.demo.opengles.egl.EGLCamera1FBOPreviewWaterMarkActivity;
import com.demo.opengles.egl.EGLCamera1PreviewActivity;
import com.demo.opengles.gaussian.GaussianComplexActivity;
import com.demo.opengles.gaussian.GaussianHorActivity;
import com.demo.opengles.gaussian.GaussianHorVerActivity;
import com.demo.opengles.gaussian.GaussianHorVerMultiActivity;
import com.demo.opengles.gaussian.GaussianVerActivity;
import com.demo.opengles.gaussian.GaussianVideoPlayerActivity;
import com.demo.opengles.gaussian.GaussianViewActivity;
import com.demo.opengles.gles3.OpenGLES3Activity;
import com.demo.opengles.graphic.BallActivity;
import com.demo.opengles.graphic.CircleActivity;
import com.demo.opengles.graphic.ConeActivity;
import com.demo.opengles.graphic.CubeActivity;
import com.demo.opengles.graphic.CylinderActivity;
import com.demo.opengles.graphic.FrameBufferActivity;
import com.demo.opengles.graphic.SquareActivity;
import com.demo.opengles.graphic.TextureActivity;
import com.demo.opengles.graphic.TextureColorfulActivity;
import com.demo.opengles.graphic.TextureEnlargeActivity;
import com.demo.opengles.graphic.TextureEnlargeMatrixActivity;
import com.demo.opengles.graphic.TextureTwoActivity;
import com.demo.opengles.graphic.TransformActivity;
import com.demo.opengles.graphic.TriangleActivity;
import com.demo.opengles.graphic.TriangleColorActivity;
import com.demo.opengles.graphic.TriangleMatrixActivity;
import com.demo.opengles.graphic.TriangleModalActivity;
import com.demo.opengles.leak.LeakGlSurfaceViewActivity;
import com.demo.opengles.light.AmbientLightActivity;
import com.demo.opengles.light.DiffuseLightActivity;
import com.demo.opengles.light.SpecularLightActivity;
import com.demo.opengles.map.MapViewGaussianActivity;
import com.demo.opengles.map.MapViewGaussianActivity2;
import com.demo.opengles.mediarecorder.camera1.surfaceview.Camera1MediaRecorderSurfaceActivity;
import com.demo.opengles.mediarecorder.camera1.textureview.Camera1MediaRecorderTextureActivity;
import com.demo.opengles.mediarecorder.camera2.background.Camera2BackgroundRecordActivity;
import com.demo.opengles.mediarecorder.camera2.surfaceview.Camera2MediaRecorderSurfaceActivity;
import com.demo.opengles.pcm.PcmRecordActivity;
import com.demo.opengles.record.camera1.EGLCamera1Record4SameTimeActivity;
import com.demo.opengles.record.camera1.EGLCamera1RecordActivity;
import com.demo.opengles.record.camera2.eglrecord.EGLCamera2GLSurfaceView4RecordActivity;
import com.demo.opengles.record.camera2.eglsurfaceview.Camera2EGLSurfaceView4PreviewActivity;
import com.demo.opengles.record.camera2.glrecord.Camera2GLSurfaceView4RecordActivity;
import com.demo.opengles.record.camera2.glrecordconcat.Camera2GLSurfaceViewConcatRecordActivity;
import com.demo.opengles.record.camera2.glrecorddouble.Camera2GLSurfaceViewDoubleRecordActivity;
import com.demo.opengles.record.camera2.glsurfaceview.Camera2GLSurfaceView4PreviewActivity;
import com.demo.opengles.record.camera2.surfaceview.Camera2SurfaceView4PreviewActivity;
import com.demo.opengles.record.camera2.takepicture.Camera2TakePictureActivity;
import com.demo.opengles.skycube.SkyCubeActivity;
import com.demo.opengles.surface.GLSurfaceViewAlphaVideoActivity;
import com.demo.opengles.surface.GLSurfaceViewVideoActivity;
import com.demo.opengles.surface.GLTextureViewAlphaVideoActivity;
import com.demo.opengles.surface.SurfaceViewActivity;
import com.demo.opengles.surface.SurfaceViewVideoActivity;
import com.demo.opengles.surface.TextureViewVideoActivity;
import com.demo.opengles.world.WorldActivity;
import com.demo.opengles.yuv.YuvNV21DisplayActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;
    private MainRecyclerAdapter adapter = new MainRecyclerAdapter();

    private List<MainItemBean> dataList = new ArrayList<MainItemBean>() {
        {
            add(new MainItemBean("?????????", TriangleActivity.class));
            add(new MainItemBean("?????????-?????????", TriangleModalActivity.class));
            add(new MainItemBean("?????????-????????????", TriangleColorActivity.class));
            add(new MainItemBean("?????????-????????????-?????????", TriangleMatrixActivity.class));
            add(new MainItemBean("?????????", SquareActivity.class));
            add(new MainItemBean("??????", CircleActivity.class));
            add(new MainItemBean("?????????", CubeActivity.class));
            add(new MainItemBean("??????", ConeActivity.class));
            add(new MainItemBean("??????", CylinderActivity.class));
            add(new MainItemBean("??????", BallActivity.class));
            add(new MainItemBean("??????-??????", TextureActivity.class));
            add(new MainItemBean("??????-???????????????", TextureTwoActivity.class));
            add(new MainItemBean("??????-????????????", TextureColorfulActivity.class));
            add(new MainItemBean("??????-?????????-???????????????", TextureEnlargeActivity.class));
            add(new MainItemBean("??????-?????????-??????????????????", TextureEnlargeMatrixActivity.class));
            add(new MainItemBean("?????????-????????????", FrameBufferActivity.class));
            add(new MainItemBean("????????????-????????????", GaussianHorActivity.class));
            add(new MainItemBean("????????????-????????????", GaussianVerActivity.class));
            add(new MainItemBean("????????????-??????????????????", GaussianHorVerActivity.class));
            add(new MainItemBean("????????????-??????????????????-??????", GaussianHorVerMultiActivity.class));
            add(new MainItemBean("????????????-?????????????????????", GaussianComplexActivity.class));
            add(new MainItemBean("????????????-????????????????????????", GaussianVideoPlayerActivity.class));
            add(new MainItemBean("????????????-?????????GaussianGlSurfaceView", GaussianViewActivity.class));
            add(new MainItemBean("????????????-????????????", MapViewGaussianActivity.class));
            add(new MainItemBean("????????????-????????????2", MapViewGaussianActivity2.class));
            add(new MainItemBean("????????????-??????-??????-??????", TransformActivity.class));
            add(new MainItemBean("Surface????????????-SurfaceView", SurfaceViewActivity.class));
            add(new MainItemBean("Surface????????????-SurfaceView????????????", SurfaceViewVideoActivity.class));
            add(new MainItemBean("Surface????????????-TextureView????????????", TextureViewVideoActivity.class));
            add(new MainItemBean("Surface????????????-GLSurfaceView????????????", GLSurfaceViewVideoActivity.class));
            add(new MainItemBean("Surface????????????-GLSurfaceView??????Alpha??????", GLSurfaceViewAlphaVideoActivity.class));
            add(new MainItemBean("Surface????????????-GLTextureView??????Alpha??????", GLTextureViewAlphaVideoActivity.class));
            add(new MainItemBean("Camera1-SurfaceView??????", Camera1SurfaceViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-TextureView??????", Camera1TextureViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-GLSurfaceView??????", Camera1GLSurfaceViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-GLSurfaceView-OpenGL???????????????", Camera1GLSurfaceViewTakePhotoActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-GLSurfaceView-??????????????????", Camera1TakePhotoGLSurfaceViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-????????????", EGLActivity.class));
            add(new MainItemBean("EGL-Camera1-??????", EGLCamera1PreviewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-Camera1-FBO-??????", EGLCamera1FBOPreviewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-Camera1-FBO-???????????????", EGLCamera1FBOPreviewWaterMarkActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-Camera1-FBO-?????????????????????????????????mp4", EGLCamera1RecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("pcm?????????????????????", PcmRecordActivity.class, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("EGL-Camera1-FBO-4??????????????????", EGLCamera1Record4SameTimeActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-SurfaceView-1?????????", Camera2TakePictureActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-SurfaceView-4?????????", Camera2SurfaceView4PreviewActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-4?????????", Camera2GLSurfaceView4PreviewActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-4?????????", Camera2GLSurfaceView4RecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-???????????????", Camera2GLSurfaceViewDoubleRecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-???????????????", Camera2GLSurfaceViewConcatRecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-EglSurfaceView-4?????????", Camera2EGLSurfaceView4PreviewActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-EglSurfaceView-4?????????", EGLCamera2GLSurfaceView4RecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("GlSurfaceView-opengl????????????", LeakGlSurfaceViewActivity.class));
            add(new MainItemBean("GlSurfaceView-????????????", WorldActivity.class));
            add(new MainItemBean("Camera1-MediaRecorder-SurfaceView", Camera1MediaRecorderSurfaceActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera1-MediaRecorder-Texture", Camera1MediaRecorderTextureActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-MediaRecorder-SurfaceView", Camera2MediaRecorderSurfaceActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("YUV-NV21????????????", YuvNV21DisplayActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-????????????????????????", Camera2BackgroundRecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("OpenGL-??????????????????-?????????", AmbientLightActivity.class));
            add(new MainItemBean("OpenGL-??????????????????-????????????", DiffuseLightActivity.class));
            add(new MainItemBean("OpenGL-??????????????????-???????????????", SpecularLightActivity.class));
            add(new MainItemBean("?????????", SkyCubeActivity.class));
            add(new MainItemBean("openGL ES 3.0", OpenGLES3Activity.class));
            add(new MainItemBean("???????????????", CubeMapActivity.class));
            add(new MainItemBean("?????????", EarthActivity.class));
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter.setDataList(dataList);

        binding.recyclerView.scrollToPosition(dataList.size() - 1);
    }

}
