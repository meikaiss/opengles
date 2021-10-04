package com.demo.opengles.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.opengles.databinding.MainItemBinding;

import java.util.List;

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
        if (bean.permissionList != null && bean.permissionList.size() > 0) {
            Activity activity = (Activity) binding.getRoot().getContext();
            if (checkSelfPermission(activity, bean.permissionList)
                    != PackageManager.PERMISSION_GRANTED) {
                String[] permissionStringArr = new String[bean.permissionList.size()];
                for (int i = 0; i < bean.permissionList.size(); i++) {
                    permissionStringArr[i] = bean.permissionList.get(i);
                }

                activity.requestPermissions(permissionStringArr, 1);
                return;
            }
        }

        Intent intent = new Intent(binding.getRoot().getContext(), bean.clz);
        intent.putExtra("title", bean.name);
        binding.getRoot().getContext().startActivity(intent);
    }


    public int checkSelfPermission(Context context, @NonNull List<String> permissionList) {
        for (int i = 0; i < permissionList.size(); i++) {
            if (ContextCompat.checkSelfPermission(context, permissionList.get(i)) != PackageManager.PERMISSION_GRANTED) {
                return PackageManager.PERMISSION_DENIED;
            }
        }
        return PackageManager.PERMISSION_GRANTED;
    }

}
