/*
 * Copyright 2021 bytedance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Create Date : 2021/6/11
 */
package com.volcengine.vertcdemo.feedshare.view;

import static com.ss.ttvideoengine.TTVideoEngine.PLAYER_OPTION_ENABLE_DATALOADER;
import static com.ss.ttvideoengine.TTVideoEngine.PLAYER_OPTION_USE_VIDEOMODEL_CACHE;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_COMMON;

import android.content.Context;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.SeekCompletionListener;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.VideoEngineSimpleCallback;
import com.ss.ttvideoengine.VideoInfoListener;
import com.ss.ttvideoengine.model.VideoInfo;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.utils.Error;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import com.volcengine.vertcdemo.feedshare.bean.VideoItem;
import com.volcengine.vertcdemo.feedshare.utils.TTSdkHelper;

import java.util.ArrayList;
import java.util.List;

public class VOLCVideoController implements VideoController, VideoInfoListener {
    private static final String TAG = "VOLCVideoController";

    private final Context mContext;
    private final VideoItem mVideoItem;
    private final List<VideoPlayListener> playListeners = new ArrayList<>(2);
    private TTVideoEngine mVideoEngine;
    private Surface mSurface;

    private boolean mPrepared;
    private boolean mPlayAfterSurfaceValid;
    private VideoAudioProcessor mAudioProcessor;

    private final SeekCompletionListener mSeekCompletionListener = new SeekCompletionListener() {
        @Override
        public void onCompletion(boolean success) {
            onSeekComplete(success);
        }
    };

    private final VideoEngineSimpleCallback mVideoEngineCallback = new VideoEngineSimpleCallback() {
        @Override
        public void onRenderStart(final TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onRenderStart");
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onRenderStart();
                }
            }
        }

        @Override
        public void onBufferStart(final int reason, final int afterFirstFrame, final int action) {
            TTVideoEngineLog.d(TAG, "onBufferStart reason " + reason
                    + ", afterFirstFrame " + afterFirstFrame
                    + ", action " + action);
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onBufferStart();
                }
            }
        }

        @Override
        public void onBufferEnd(final int code) {
            TTVideoEngineLog.d(TAG, "onBufferEnd code " + code);
            /*for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onBufferEnd();
                }
            }*/
        }

        @Override
        public void onPlaybackStateChanged(TTVideoEngine engine, int playbackState) {
            TTVideoEngineLog.d(TAG, "onPlaybackStateChanged " + playbackState);
            switch (playbackState) {
                case TTVideoEngine.PLAYBACK_STATE_PLAYING:
                    for (VideoPlayListener listener : playListeners) {
                        if (listener != null) {
                            listener.onVideoPlay();
                        }
                    }
                    break;
                case TTVideoEngine.PLAYBACK_STATE_PAUSED:
                    for (VideoPlayListener listener : playListeners) {
                        if (listener != null) {
                            listener.onVideoPause();
                        }
                    }
                default:
                    break;
            }
        }

        @Override
        public void onVideoSizeChanged(TTVideoEngine engine, int width, int height) {
            TTVideoEngineLog.d(TAG, "onVideoSizeChanged width " + width + ", height " + height);
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onVideoSizeChanged(width, height);
                }
            }
        }

        @Override
        public void onBufferingUpdate(TTVideoEngine engine, int percent) {
            TTVideoEngineLog.d(TAG, "onBufferingUpdate percent " + percent);
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onBufferingUpdate(percent);
                }
            }
        }

        @Override
        public void onPrepare(TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onPrepare");
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onPrepare();
                }
            }
        }

        @Override
        public void onPrepared(TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onPrepared");
            mPrepared = true;
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onPrepared();
                }
            }
        }

        @Override
        public void onStreamChanged(TTVideoEngine engine, int type) {
            TTVideoEngineLog.d(TAG, "onStreamChanged type " + type);
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onStreamChanged(type);
                }
            }
        }

        @Override
        public void onCompletion(TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onCompletion");
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onVideoCompleted();
                }
            }
        }

        @Override
        public void onError(Error error) {
            TTVideoEngineLog.d(TAG, "onError error " + error);
            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onError(mVideoItem, error);
                }
            }
        }
    };

    public VOLCVideoController(@NonNull Context context, @NonNull VideoItem mVideoItem, VideoPlayListener listener) {
        this.mContext = context;
        this.mVideoItem = mVideoItem;
        this.playListeners.add(listener);
    }

    public void addPlayListener(VideoPlayListener listener) {
        this.playListeners.add(listener);
    }

    public void setAudioProcessor(VideoAudioProcessor processor) {
        mAudioProcessor = processor;
    }

    private void initEngine() {
        if (mVideoEngine == null) {
            // VOD key step play 1: init TTVideoEngine with ApplicationContext
            mVideoEngine = new TTVideoEngine(mContext.getApplicationContext(), TTVideoEngine.PLAYER_TYPE_OWN);
            // VOD key step play 2: set Callback
            mVideoEngine.setVideoEngineSimpleCallback(mVideoEngineCallback);
            mVideoEngine.setVideoInfoListener(this);
            // VOD key step play 3: use mdl
            mVideoEngine.setIntOption(PLAYER_OPTION_ENABLE_DATALOADER, 1);

            // VOD key step play 8: other feature
            // use videomodel cache
            mVideoEngine.setIntOption(PLAYER_OPTION_USE_VIDEOMODEL_CACHE, 1);
            // use h265
            mVideoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_h265,
                    TTSdkHelper.videoEnableH265() ? 1 : 0
            );
            // use video hardware
            mVideoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABEL_HARDWARE_DECODE,
                    TTSdkHelper.enableVideoHW() ? 1 : 0);

            // Loop Playback
            mVideoEngine.setLooping(true);
            // FIXME: 2022/5/20
            /*if (BuildConfig.DEBUG) {
                // open debug log
                mVideoEngine.setIntOption(PLAYER_OPTION_OUTPUT_LOG, 1);
            }*/
            // enable key message upload：default is enable
            mVideoEngine.setReportLogEnable(TTSdkHelper.engineEnableUploadLog());
            DataLoaderHelper.getDataLoader().setReportLogEnable(TTSdkHelper.mdlEnableUploadLog());

            // set resolution
            mVideoEngine.configResolution(Resolution.High);

            //全局打开通用策略，需要在主线程调用
            TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_COMMON, STRATEGY_SCENE_SMALL_VIDEO);

            // VOD key step play 4: set source
            mVideoEngine.setStrategySource(mVideoItem.playStrategySource);

            for (VideoPlayListener listener : playListeners) {
                if (listener != null) {
                    listener.onCallPlay();
                }
            }
            if (mAudioProcessor != null) {
                mVideoEngine.setAudioProcessor(mAudioProcessor);
                //控制播放器不进行音频渲染
                mVideoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_SET_VOICE, TTVideoEngine.VOICE_DUMMY);
                mVideoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_DUMMY_AUDIO_SLEEP, 0);
            }
        }
    }

    @Override
    public int getDuration() {
        if (mPrepared && mVideoEngine != null) {
            return mVideoEngine.getDuration();
        }
        return (int) mVideoItem.duration;
    }

    public void play() {
        if (mVideoItem.playStrategySource == null) {
            Log.i(TAG, "当前视频对应的播放数据为空");
            return;
        }
        initEngine();
        if (mSurface != null && mSurface.isValid()) {
            // VOD key step play 5: set surface
            mVideoEngine.setSurface(mSurface);
            // VOD key step play 6: play
            mVideoEngine.play();
        } else {
            mPlayAfterSurfaceValid = true;
        }
    }

    public void pause() {
        if (mVideoEngine != null) {
            mVideoEngine.pause();
        }
    }

    public void release() {
        if (mVideoEngine == null) {
            return;
        }
        mVideoEngine.setAudioProcessor(null);
        for (VideoPlayListener listener : playListeners) {
            if (listener != null) {
                listener.onVideoPreRelease();
            }
        }
        mPrepared = false;
        // VOD key step play 7: release
        mVideoEngine.release();
        mVideoEngine = null;
        for (VideoPlayListener listener : playListeners) {
            if (listener != null) {
                listener.onVideoReleased();
            }
        }
    }

    @Override
    public void mute() {
        if (mVideoEngine != null) {
            mVideoEngine.setIsMute(true);
        }
    }

    @Override
    public boolean isPlaying() {
        return mPrepared && mVideoEngine.getPlaybackState() == TTVideoEngine.PLAYBACK_STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mPrepared && mVideoEngine.getPlaybackState() == TTVideoEngine.PLAYBACK_STATE_PAUSED;
    }

    @Override
    public boolean isLooping() {
        if (mVideoEngine == null) {
            return false;
        }
        return mVideoEngine.isLooping();
    }

    @Override
    public String getCover() {
        return mVideoItem.coverUrl;
    }

    @Override
    public int getVideoWidth() {
        if (mVideoEngine == null) {
            return 0;
        }
        return mVideoEngine.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        if (mVideoEngine == null) {
            return 0;
        }
        return mVideoEngine.getVideoHeight();
    }

    @Override
    public int getCurrentPlaybackTime() {
        if (mVideoEngine == null) {
            return 0;
        }
        return mVideoEngine.getCurrentPlaybackTime();
    }

    @Override
    public void notifyProgressUpdate(int progress) {
        if (mVideoEngine == null) {
            return;
        }
        for (VideoPlayListener listener : playListeners) {
            if (listener != null) {
                listener.onProgressUpdate(progress, getDuration());
            }
        }
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mSurface == null || !mSurface.isValid()) {
            mSurface = null;
        }

        if (mVideoEngine != null) {
            mVideoEngine.setSurface(mSurface);

            if (mSurface != null && mPlayAfterSurfaceValid) {
                mVideoEngine.play();
                mPlayAfterSurfaceValid = false;
            }
        }
    }

    public TTVideoEngine getTTVideoEngine() {
        return mVideoEngine;
    }

    @Override
    public boolean onFetchedVideoInfo(final VideoModel videoModel) {
        if (videoModel == null) {
            return false;
        }

        final List<VideoInfo> videoInfoList = videoModel.getVideoInfoList();
        if (videoInfoList != null && videoInfoList.size() > 0) {
            final VideoInfo videoInfo = videoInfoList.get(0);
            if (videoInfo != null) {
                int width = videoInfo.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VWIDTH);
                int height = videoInfo.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VHEIGHT);
                if (width > 0 && height > 0) {
                    for (VideoPlayListener listener : playListeners) {
                        if (listener != null) {
                            listener.onFetchVideoModel(width, height);
                        }
                    }
                }
            }
        }

        return false;
    }

    public void seekTo(int msec) {
        if (mVideoEngine == null) {
            return;
        }
        mVideoEngine.seekTo(msec, mSeekCompletionListener);
        for (VideoPlayListener listener : playListeners) {
            if (listener != null) {
                listener.onVideoSeekStart(msec);
            }
        }
    }

    private void onSeekComplete(final boolean success) {
        TTVideoEngineLog.d(TAG, "seek_complete:" + (success ? "done" : "fail"));
        for (VideoPlayListener listener : playListeners) {
            if (listener != null) {
                listener.onVideoSeekComplete(success);
            }
        }
    }

    public void setMixAudioGain(@IntRange(from = 0, to = 200) int gain) {
        final VideoAudioProcessor audioProcessor = mAudioProcessor;
        if (audioProcessor != null) {
            audioProcessor.setMixAudioGain(gain);
        }
    }
}
