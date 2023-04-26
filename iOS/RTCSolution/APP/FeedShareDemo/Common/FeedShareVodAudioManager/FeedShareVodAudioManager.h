// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
@class ByteRTCVideo;

/// Audio mixingID default 3001
extern int FeedShareVodAudioProcessorAudioMixingID;

@interface FeedShareVodAudioManager : NSObject

/// Initialize
/// @param rtcKit ByteRTCVideo
- (instancetype)initWithRTCKit:(ByteRTCVideo *)rtcKit;

/// Open audio
/// @param samplerate Samplerate
/// @param channels Channels
- (void)openAudio:(int)samplerate channels:(int)channels;

/// Process audio
/// @param inouts Inouts
/// @param samples Samples
- (void)processAudio:(float **)inouts samples:(int)samples;


@end

