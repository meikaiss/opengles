package com.demo.opengles.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.opengles.databinding.MainItemBinding;

public class MainViewHolder extends RecyclerView.ViewHolder {

    private MainItemBinding binding;
    private MainItemBean bean;

    public MainViewHolder(MainItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTarget();
            }
        });
    }

    public void bind(MainItemBean bean) {
        binding.tvName.setText(bean.name);
        this.bean = bean;
    }

    public void openTarget() {
        if (bean.permission != null) {
            Activity activity = (Activity) binding.getRoot().getContext();
            if (ContextCompat.checkSelfPermission(activity, bean.permission)
                    != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{bean.permission}, 1);
                return;
            }
        }

        Intent intent = new Intent(binding.getRoot().getContext(), bean.clz);
        binding.getRoot().getContext().startActivity(intent);
    }

}
