// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.utils;

import androidx.annotation.IntRange;

import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.audio.IAudioMixingManager;
import com.ss.bytertc.engine.data.AudioChannel;
import com.ss.bytertc.engine.data.AudioMixingType;
import com.ss.bytertc.engine.data.AudioSampleRate;
import com.ss.bytertc.engine.utils.AudioFrame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VodAudioProcessor {

    public static final int DEFAULT_VIDEO_AUDIO_GAIN = 20;
    public static final int DEFAULT_RTC_AUDIO_GAIN = 100;

    @IntRange(from = 0, to = 200)
    private int mixAudioGain = DEFAULT_VIDEO_AUDIO_GAIN;

    private int mChannelCount;
    private int mSampleRate;
    private RTCVideo mEngine;
    private IAudioMixingManager mixAudioManager;
    private ByteBuffer mRTCBuffer;

    public VodAudioProcessor(RTCVideo engine) {
        mEngine = engine;
        mixAudioManager = mEngine.getAudioMixingManager();
        mixAudioManager.enableAudioMixingFrame(0, AudioMixingType.AUDIO_MIXING_TYPE_PLAYOUT);
    }

    public void audioOpen(int sampleRate, int channelCount) {
        this.mChannelCount = channelCount;
        this.mSampleRate = sampleRate;
    }

    public void audioProcess(ByteBuffer[] byteBuffers, int samples, long timestamp) {
        if (mEngine == null) {
            return;
        }
        int channelsCount = mChannelCount;
        // S16 格式一个声道中每个采样占两个字节
        int length = samples * channelsCount * 2;
        if (mRTCBuffer == null || mRTCBuffer.capacity() < length) {
            mRTCBuffer = ByteBuffer.allocate(length);
            mRTCBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        mRTCBuffer.clear();
        ByteBuffer srcBuffer0 = byteBuffers[0];
        srcBuffer0.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer srcBuffer1 = null;
        if (channelsCount > 1) {
            srcBuffer1 = byteBuffers[1];
            srcBuffer1.order(ByteOrder.LITTLE_ENDIAN);
        }
        // S16 双声道为 左右左右...
        for (int i = 0; i < samples; i++) {
            oneSampleFLTPtoS16(srcBuffer0, mRTCBuffer);
            if (srcBuffer1 != null) {
                oneSampleFLTPtoS16(srcBuffer1, mRTCBuffer);
            }
        }

        AudioChannel channel = getAudioChannel(mChannelCount);
        AudioSampleRate sampleRate = getAudioSampleRate(mSampleRate);
        AudioFrame frame = new AudioFrame(mRTCBuffer.array(), samples, sampleRate, channel);
        if (mixAudioManager != null) {
            mixAudioManager.setAudioMixingVolume(0, mixAudioGain, AudioMixingType.AUDIO_MIXING_TYPE_PLAYOUT);
            mixAudioManager.pushAudioMixingFrame(0, frame);
        }
    }

    public void setMixAudioGain(@IntRange(from = 0, to = 200) int gain) {
        mixAudioGain = gain;
    }

    /*
     * 将一个采样的数据格式从 FLTP 转换为 S16
     */
    private void oneSampleFLTPtoS16(ByteBuffer srcBuffer, ByteBuffer dstBuffer) {
        float sample = srcBuffer.getFloat();
        int value = (int) (1.0f * sample * Short.MAX_VALUE);
        if (value > Short.MAX_VALUE) {
            value = Short.MAX_VALUE;
        } else if (value < Short.MIN_VALUE) {
            value = Short.MIN_VALUE;
        }
        dstBuffer.putShort((short) (value));
    }

    private AudioSampleRate getAudioSampleRate(int sampleRate) {
        if (sampleRate == 8000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_8000;
        } else if (sampleRate == 16000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_16000;
        } else if (sampleRate == 32000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_32000;
        } else if (sampleRate == 44100) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_44100;
        } else if (sampleRate == 48000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_48000;
        }
        return AudioSampleRate.AUDIO_SAMPLE_RATE_AUTO;
    }

    private AudioChannel getAudioChannel(int channelCount) {
        if (channelCount == 1) {
            return AudioChannel.AUDIO_CHANNEL_MONO;
        } else if (channelCount >= 2) {
            return AudioChannel.AUDIO_CHANNEL_STEREO;
        }
        throw new IllegalArgumentException("channelCount error: " + channelCount);
    }

}
