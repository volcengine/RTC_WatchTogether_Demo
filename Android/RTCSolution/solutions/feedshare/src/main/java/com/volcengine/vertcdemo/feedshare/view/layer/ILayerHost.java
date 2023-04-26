// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layer;

import android.content.Context;
import android.view.ViewGroup;

import com.volcengine.vertcdemo.feedshare.view.VideoController;

public interface ILayerHost {

    void addLayer(ILayer layer);

    void removeLayer(ILayer layer);

    ILayer getLayer(int layerType);

    void refreshLayers();

    int findPositionForLayer(ILayer layer, ViewGroup rootView);

    boolean notifyEvent(IVideoLayerEvent event);

    void execCommand(IVideoLayerCommand command);

    Context getContext();

    String getCover();

    boolean isPaused();

    VideoController getVideoController();
}
