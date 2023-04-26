// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.feature.feedshare;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.volcengine.vertcdemo.feedshare.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter.ViewHolder> {
    private final List<T> mDatas = new ArrayList<>();

    public BaseAdapter(List<T> datas) {
        if (datas != null) {
            mDatas.addAll(datas);
        }
    }

    public abstract int getLayoutId(int viewType);

    public abstract void onBindViewHolder(ViewHolder holder, T data, int position);

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutId(viewType), parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        onBindViewHolder(holder, mDatas.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    public void setData(List<T> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
    }

    public void replaceItem(int index, T item) {
        mDatas.set(index, item);
    }

    public void replaceAll(List<T> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void addAll(List<T> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public List<T> getAll() {
        return mDatas;
    }

    public int getPosition(String id) {
        return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View mItemView;
        private final SparseArray<View> mViews = new SparseArray<>();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
            mItemView.setTag(R.id.holder_tag, this);
        }

        public <T extends View> T getView(@IdRes int resourceId) {
            View view = mViews.get(resourceId);
            if (view == null) {
                T t = mItemView.findViewById(resourceId);
                mViews.put(resourceId, t);
                view = t;
            }
            return (T) view;
        }
    }
}



