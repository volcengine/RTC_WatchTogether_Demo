// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.view.layer.BaseVideoLayer;
import com.volcengine.vertcdemo.feedshare.view.layer.CommonLayerCommand;
import com.volcengine.vertcdemo.feedshare.view.layer.ILayer;
import com.volcengine.vertcdemo.feedshare.view.layer.IVideoLayerCommand;
import com.volcengine.vertcdemo.feedshare.view.layer.IVideoLayerEvent;

import java.util.Arrays;
import java.util.List;

public class LoadFailLayer extends BaseVideoLayer {
    @Override
    public int getZIndex() {
        return ILayer.LOAD_FAIL_Z_INDEX;
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return Arrays.asList(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR);
    }

    @Override
    protected void setupViews() {
        mLayerView.setVisibility(View.GONE);
        mLayerView.findViewById(R.id.tv_click_to_retry).setOnClickListener(v -> {
            dismissView();
            mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_REPLY));
        });

        mLayerView.setOnClickListener(
                v -> mLayerView.findViewById(R.id.errorMsg).setVisibility(View.VISIBLE));
    }

    @SuppressLint("InflateParams")
    @Override
    protected View getLayerView(final Context context, @NonNull final LayoutInflater inflater) {
        return inflater.inflate(R.layout.layer_load_fail, null);
    }

    @Override
    public void refresh() {
        dismissView();
    }

    @Override
    public boolean handleVideoEvent(@NonNull final IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR:
                final Error param = event.getParam(Error.class);
                showFailedView(param);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_START:
                dismissView();
                break;
            default:
                break;
        }
        return false;
    }

    private void showFailedView(Error error) {
        final TextView tvErrorMsg = mLayerView.findViewById(R.id.errorMsg);
        tvErrorMsg.setText(error == null ? "加载视频出错!" : error.toString());
        mLayerView.setVisibility(View.GONE);
        mLayerView.setVisibility(View.VISIBLE);
    }

    private void dismissView() {
        final TextView tvErrorMsg = mLayerView.findViewById(R.id.errorMsg);
        tvErrorMsg.setText(null);
        mLayerView.setVisibility(View.GONE);
        mLayerView.setVisibility(View.GONE);
    }
}
