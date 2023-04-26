// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view;

import com.ss.ttvideoengine.utils.Error;
import com.volcengine.vertcdemo.feedshare.bean.VideoItem;

public interface VideoPlayListener {

    void onVideoSizeChanged(int width, int height);

    void onCallPlay();

    void onPrepare();

    void onPrepared();

    void onRenderStart();

    void onVideoPlay();

    void onVideoPause();

    void onBufferStart();

    void onBufferingUpdate(int percent);

    void onBufferEnd();

    void onStreamChanged(int type);

    void onVideoCompleted();

    void onVideoPreRelease();

    void onVideoReleased();

    void onError(VideoItem videoItem, Error error);

    void onFetchVideoModel(final int videoWidth, final int videoHeight);

    void onVideoSeekComplete(boolean success);

    void onVideoSeekStart(int msec);

    void onProgressUpdate(int progress,int duration);
}
