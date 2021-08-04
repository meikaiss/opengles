package com.demo.opengles.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.opengles.databinding.MainItemBinding;

import java.util.List;

public class MainRecyclerAdapter extends RecyclerView.Adapter {

    private List<MainItemBean> dataList;

    public void setDataList(List<MainItemBean> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MainItemBinding binding =
                MainItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        MainViewHolder holder = new MainViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MainViewHolder mainViewHolder = (MainViewHolder) holder;

        mainViewHolder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

}
