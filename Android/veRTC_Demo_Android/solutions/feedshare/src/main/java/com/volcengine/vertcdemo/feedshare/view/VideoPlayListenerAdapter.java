package com.volcengine.vertcdemo.feedshare.view;

import com.ss.ttvideoengine.utils.Error;
import com.volcengine.vertcdemo.feedshare.bean.VideoItem;

public abstract class VideoPlayListenerAdapter implements VideoPlayListener {
    @Override
    public void onVideoSizeChanged(int width, int height) {

    }

    @Override
    public void onCallPlay() {

    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onRenderStart() {

    }

    @Override
    public void onVideoPlay() {

    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onBufferStart() {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onBufferEnd() {

    }

    @Override
    public void onStreamChanged(int type) {

    }

    @Override
    public void onVideoCompleted() {

    }

    @Override
    public void onVideoPreRelease() {

    }

    @Override
    public void onVideoReleased() {

    }

    @Override
    public void onError(VideoItem videoItem, Error error) {

    }

    @Override
    public void onFetchVideoModel(int videoWidth, int videoHeight) {

    }

    @Override
    public void onVideoSeekComplete(boolean success) {

    }

    @Override
    public void onVideoSeekStart(int msec) {

    }

    @Override
    public void onProgressUpdate(int progress, int duration) {

    }
}
