package com.volcengine.vertcdemo.feedshare.view;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.ttm.player.AudioProcessor;
import com.volcengine.vertcdemo.feedshare.utils.VodAudioProcessor;

import java.nio.ByteBuffer;

public class VideoAudioProcessor extends AudioProcessor {
    @NonNull
    private final VodAudioProcessor processor;

    public VideoAudioProcessor(RTCVideo engine) {
        processor = new VodAudioProcessor(engine);
    }

    @Override
    public void audioOpen(int sampleRate, int channelCount, int duration, int format) {
        processor.audioOpen(sampleRate, channelCount);
    }

    @Override
    public void audioProcess(ByteBuffer[] byteBuffers, int samples, long timestamp) {
        processor.audioProcess(byteBuffers, samples, timestamp);
    }

    @Override
    public void audioClose() {

    }

    @Override
    public void audioRelease(int i) {

    }

    public void setMixAudioGain(@IntRange(from = 0, to = 200) int gain) {
        processor.setMixAudioGain(gain);
    }
}