// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layer;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import java.util.List;

public interface ILayer extends Comparable<ILayer> {

    int VIDEO_COVER_Z_INDEX = 300;

    int LOADING_Z_INDEX = 400;

    int SMALL_TOOLBAR_Z_INDEX = 600;

    int LOAD_FAIL_Z_INDEX = 1200;

    int DEBUG_TOOL_Z_INDEX = 1400;

    Pair<View, RelativeLayout.LayoutParams> onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater);

    @NonNull
    List<Integer> getSupportEvents();

    void refresh();

    int getZIndex();

    void onRegister(ILayerHost host);

    void onUnregister(ILayerHost host);

    boolean handleVideoEvent(@NonNull IVideoLayerEvent event);
}
