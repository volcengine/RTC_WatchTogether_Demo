package com.volcengine.vertcdemo.feedshare.view;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.ttm.player.AudioProcessor;
import com.volcengine.vertcdemo.feedshare.utils.VodAudioProcessor;

import java.nio.ByteBuffer;

public class VideoAudioProcessor extends AudioProcessor {
    private final VodAudioProcessor processor;

    public VideoAudioProcessor(RTCEngine engine) {
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
}