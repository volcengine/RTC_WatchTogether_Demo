// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "FeedShareVodAudioManager.h"
#import "ToolKit.h"

int FeedShareVodAudioProcessorAudioMixingID = 3001;

@interface FeedShareVodAudioManager ()

@property (nonatomic, weak) ByteRTCVideo *rtcKit;
@property (nonatomic, assign) int length;

@end

@implementation FeedShareVodAudioManager {
    int16_t* buffer;
    int _samplerate;
    int _channels;
}

- (instancetype)initWithRTCKit:(ByteRTCVideo *)rtcKit {
    if (self = [super init]) {
        self.rtcKit = rtcKit;
    }
    return self;
}

- (void)openAudio:(int)samplerate channels:(int)channels {
    _samplerate = samplerate;
    _channels = channels;
}

- (void)processAudio:(float **)inouts samples:(int)samples {
    
    int channelsCount = MIN(2, _channels);
    float gain = 1.0;
    int samplerate = _samplerate;
    
    int length = samples * channelsCount;
    if (self.length < length) {
        if (self.length > 0) {
            self.length = 0;
            delete [] (buffer);
        }
        buffer = new int16_t[length*2];
        self.length = length*2;
    }
    
    for (int i = 0; i < channelsCount; i++) {
        int offset = i;
        float *dataList = inouts[i];
        for (int j = 0; j < samples; j++) {
            float data = dataList[j];
            int value = gain * data * INT16_MAX;
            if(value > INT16_MAX){
                value = INT16_MAX;
            } else if(value < INT16_MIN){
                value = INT16_MIN;
            }
            buffer[offset] = value;
            offset += channelsCount;
        }
    }
    
    ByteRTCAudioFrame *frame = [[ByteRTCAudioFrame alloc] init];
    frame.buffer = [NSData dataWithBytes:buffer length:length*2]; // 用类方法，不参与buffer生命周期管理
    
    frame.samples = samples;
    frame.channel = (ByteRTCAudioChannel)channelsCount;
    frame.sampleRate = (ByteRTCAudioSampleRate)samplerate;
    
    ByteRTCAudioMixingManager *manager = [self.rtcKit getAudioMixingManager];
    [manager pushAudioMixingFrame:FeedShareVodAudioProcessorAudioMixingID audioFrame:frame];
}

- (void)dealloc {
    if (self.length > 0) {
        delete [] buffer;
        self.length = 0;
    }
}

@end
