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
import com.demo.opengles.databinding.MainActivityBinding;
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
import com.demo.opengles.map.MapViewGaussianActivity;
import com.demo.opengles.map.MapViewGaussianActivity2;
import com.demo.opengles.pcm.PcmRecordActivity;
import com.demo.opengles.record.camera1.EGLCamera1Record4SameTimeActivity;
import com.demo.opengles.record.camera1.EGLCamera1RecordActivity;
import com.demo.opengles.mediarecorder.camera1.surfaceview.Camera1MediaRecorderSurfaceActivity;
import com.demo.opengles.mediarecorder.camera1.textureview.Camera1MediaRecorderTextureActivity;
import com.demo.opengles.record.camera2.eglrecord.EGLCamera2GLSurfaceView4RecordActivity;
import com.demo.opengles.record.camera2.eglsurfaceview.Camera2EGLSurfaceView4PreviewActivity;
import com.demo.opengles.record.camera2.glrecord.Camera2GLSurfaceView4RecordActivity;
import com.demo.opengles.record.camera2.glrecordconcat.Camera2GLSurfaceViewConcatRecordActivity;
import com.demo.opengles.record.camera2.glrecorddouble.Camera2GLSurfaceViewDoubleRecordActivity;
import com.demo.opengles.record.camera2.glsurfaceview.Camera2GLSurfaceView4PreviewActivity;
import com.demo.opengles.mediarecorder.camera2.surfaceview.Camera2MediaRecorderSurfaceActivity;
import com.demo.opengles.record.camera2.surfaceview.Camera2SurfaceView4PreviewActivity;
import com.demo.opengles.surface.GLSurfaceViewAlphaVideoActivity;
import com.demo.opengles.surface.GLSurfaceViewVideoActivity;
import com.demo.opengles.surface.GLTextureViewAlphaVideoActivity;
import com.demo.opengles.surface.SurfaceViewActivity;
import com.demo.opengles.surface.SurfaceViewVideoActivity;
import com.demo.opengles.surface.TextureViewVideoActivity;
import com.demo.opengles.world.WorldActivity;
import com.demo.opengles.yuv.YuvDisplayActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;
    private MainRecyclerAdapter adapter = new MainRecyclerAdapter();

    private List<MainItemBean> dataList = new ArrayList<MainItemBean>() {
        {
            add(new MainItemBean("三角形", TriangleActivity.class));
            add(new MainItemBean("三角形-模运算", TriangleModalActivity.class));
            add(new MainItemBean("三角形-颜色属性", TriangleColorActivity.class));
            add(new MainItemBean("三角形-矩阵变换-无拉伸", TriangleMatrixActivity.class));
            add(new MainItemBean("正方形", SquareActivity.class));
            add(new MainItemBean("圆形", CircleActivity.class));
            add(new MainItemBean("正方体", CubeActivity.class));
            add(new MainItemBean("圆锥", ConeActivity.class));
            add(new MainItemBean("圆柱", CylinderActivity.class));
            add(new MainItemBean("球体", BallActivity.class));
            add(new MainItemBean("纹理-贴图", TextureActivity.class));
            add(new MainItemBean("纹理-双纹理贴图", TextureTwoActivity.class));
            add(new MainItemBean("纹理-色彩变换", TextureColorfulActivity.class));
            add(new MainItemBean("纹理-放大镜-无投影变换", TextureEnlargeActivity.class));
            add(new MainItemBean("纹理-放大镜-正交投影变换", TextureEnlargeMatrixActivity.class));
            add(new MainItemBean("帧缓冲-离屏渲染", FrameBufferActivity.class));
            add(new MainItemBean("高斯模糊-水平方向", GaussianHorActivity.class));
            add(new MainItemBean("高斯模糊-垂直方向", GaussianVerActivity.class));
            add(new MainItemBean("高斯模糊-水平垂直同时", GaussianHorVerActivity.class));
            add(new MainItemBean("高斯模糊-水平垂直同时-多层", GaussianHorVerMultiActivity.class));
            add(new MainItemBean("高斯模糊-多层不规则形状", GaussianComplexActivity.class));
            add(new MainItemBean("高斯模糊-视频播放高斯模糊", GaussianVideoPlayerActivity.class));
            add(new MainItemBean("高斯模糊-自定义GaussianGlSurfaceView", GaussianViewActivity.class));
            add(new MainItemBean("高斯模糊-高德地图", MapViewGaussianActivity.class));
            add(new MainItemBean("高斯模糊-高德地图2", MapViewGaussianActivity2.class));
            add(new MainItemBean("位置变换-平移-旋转-缩放", TransformActivity.class));
            add(new MainItemBean("Surface绘图表面-SurfaceView", SurfaceViewActivity.class));
            add(new MainItemBean("Surface绘图表面-SurfaceView播放视频", SurfaceViewVideoActivity.class));
            add(new MainItemBean("Surface绘图表面-TextureView播放视频", TextureViewVideoActivity.class));
            add(new MainItemBean("Surface绘图表面-GLSurfaceView播放视频", GLSurfaceViewVideoActivity.class));
            add(new MainItemBean("Surface绘图表面-GLSurfaceView播放Alpha视频", GLSurfaceViewAlphaVideoActivity.class));
            add(new MainItemBean("Surface绘图表面-GLTextureView播放Alpha视频", GLTextureViewAlphaVideoActivity.class));
            add(new MainItemBean("Camera1-SurfaceView预览", Camera1SurfaceViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-TextureView预览", Camera1TextureViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-GLSurfaceView预览", Camera1GLSurfaceViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-GLSurfaceView-OpenGL读像素拍照", Camera1GLSurfaceViewTakePhotoActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("Camera1-GLSurfaceView-相机硬件拍照", Camera1TakePhotoGLSurfaceViewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-基本环境", EGLActivity.class));
            add(new MainItemBean("EGL-Camera1-预览", EGLCamera1PreviewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-Camera1-FBO-预览", EGLCamera1FBOPreviewActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-Camera1-FBO-预览加水印", EGLCamera1FBOPreviewWaterMarkActivity.class, Manifest.permission.CAMERA));
            add(new MainItemBean("EGL-Camera1-FBO-视频音频同时录制并保存mp4", EGLCamera1RecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("pcm录制并保存文件", PcmRecordActivity.class, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("EGL-Camera1-FBO-4路录制音视频", EGLCamera1Record4SameTimeActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-SurfaceView-4路预览", Camera2SurfaceView4PreviewActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-4路预览", Camera2GLSurfaceView4PreviewActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-4路录制", Camera2GLSurfaceView4RecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-一拖二录制", Camera2GLSurfaceViewDoubleRecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-GLSurfaceView-四合一录制", Camera2GLSurfaceViewConcatRecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-EglSurfaceView-4路预览", Camera2EGLSurfaceView4PreviewActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-EglSurfaceView-4路录制", EGLCamera2GLSurfaceView4RecordActivity.class, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("GlSurfaceView-opengl资源释放", LeakGlSurfaceViewActivity.class));
            add(new MainItemBean("GlSurfaceView-三维世界", WorldActivity.class));
            add(new MainItemBean("Camera1-MediaRecorder-SurfaceView", Camera1MediaRecorderSurfaceActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera1-MediaRecorder-Texture", Camera1MediaRecorderTextureActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("Camera2-MediaRecorder-SurfaceView", Camera2MediaRecorderSurfaceActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
            add(new MainItemBean("YUV图像显示", YuvDisplayActivity.class, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO));
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
