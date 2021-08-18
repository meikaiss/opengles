package com.demo.opengles.main;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.demo.opengles.databinding.MainActivityBinding;
import com.demo.opengles.graphic.BallActivity;
import com.demo.opengles.graphic.CircleActivity;
import com.demo.opengles.graphic.ConeActivity;
import com.demo.opengles.graphic.CubeActivity;
import com.demo.opengles.graphic.CylinderActivity;
import com.demo.opengles.graphic.SquareActivity;
import com.demo.opengles.graphic.TextureActivity;
import com.demo.opengles.graphic.TextureColorfulActivity;
import com.demo.opengles.graphic.TextureEnlargeMatrixActivity;
import com.demo.opengles.graphic.TextureEnlargeActivity;
import com.demo.opengles.graphic.TriangleActivity;
import com.demo.opengles.graphic.TriangleColorActivity;
import com.demo.opengles.graphic.TriangleMatrixActivity;
import com.demo.opengles.graphic.TriangleModalActivity;

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
            add(new MainItemBean("纹理-色彩变换", TextureColorfulActivity.class));
            add(new MainItemBean("纹理-放大镜-无投影变换", TextureEnlargeActivity.class));
            add(new MainItemBean("纹理-放大镜-正交投影变换", TextureEnlargeMatrixActivity.class));
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        adapter.setDataList(dataList);

    }

}
