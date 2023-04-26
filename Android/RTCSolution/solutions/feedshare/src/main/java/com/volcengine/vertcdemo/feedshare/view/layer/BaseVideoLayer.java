// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layer;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

public abstract class BaseVideoLayer implements ILayer {

    protected View mLayerView;
    protected ILayerHost mHost;
    protected Context mContext;

    @Override
    public void onRegister(ILayerHost host) {
        mHost = host;
    }

    @Override
    public void onUnregister(ILayerHost host) {
        if (mLayerView != null) {
            mLayerView.setOnClickListener(null);
        }
        mHost = null;
    }

    @Override
    public Pair<View, RelativeLayout.LayoutParams> onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContext = context;
        if (mLayerView == null) {
            mLayerView = getLayerView(context, inflater);
            if (mLayerView != null) {
                setupViews();
            }
        }

        refresh();
        return new Pair<>(mLayerView, params);
    }

    protected abstract View getLayerView(final Context context, @NonNull LayoutInflater inflater);

    @Override
    public void refresh() {
    }

    protected void setupViews() {
    }

    @Override
    public int compareTo(@NonNull ILayer another) {
        return Integer.compare(getZIndex(), another.getZIndex());
    }
}
