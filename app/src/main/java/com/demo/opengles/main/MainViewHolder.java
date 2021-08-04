package com.demo.opengles.main;

import android.content.Intent;
import android.view.View;

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
        Intent intent = new Intent(binding.getRoot().getContext(), bean.clz);
        binding.getRoot().getContext().startActivity(intent);
    }

}
