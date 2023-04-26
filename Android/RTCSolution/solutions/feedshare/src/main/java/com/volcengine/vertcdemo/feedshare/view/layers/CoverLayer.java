// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.volcengine.vertcdemo.feedshare.view.layer.BaseVideoLayer;
import com.volcengine.vertcdemo.feedshare.view.layer.ILayer;
import com.volcengine.vertcdemo.feedshare.view.layer.IVideoLayerEvent;

import java.util.Collections;
import java.util.List;

public class CoverLayer extends BaseVideoLayer {
    private static final String TAG = "CoverLayer";

    private ImageView mCoverView;

    private final List<Integer> mSupportEvents =
            Collections.singletonList(IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START);

    @Override
    public int getZIndex() {
        return ILayer.VIDEO_COVER_Z_INDEX;
    }

    @Override
    protected View getLayerView(final Context context, @NonNull LayoutInflater inflater) {
        mCoverView = new ImageView(context);
        mCoverView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mCoverView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
        return mCoverView;
    }

    @Override
    public void refresh() {
        show();
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return mSupportEvents;
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    private void show() {
        Log.d(TAG, "show");
        if (mCoverView == null) {
            return;
        }
        mCoverView.setVisibility(View.VISIBLE);
        if (mHost == null) {
            return;
        }
        final String cover = mHost.getCover();
        Glide.with(mCoverView).load(cover).into(mCoverView);
    }

    private void hide() {
        Log.d(TAG, "hide");
        if (mCoverView == null) {
            return;
        }
        mCoverView.setVisibility(View.GONE);
    }
}
