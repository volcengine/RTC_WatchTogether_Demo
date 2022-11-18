//
//  VideoAudioProcesser.h
//  ByteRTC
//
//  Created by bytedance on 2022/2/10.
//

#import <Foundation/Foundation.h>
@class ByteRTCVideo;

/// Audio mixingID default 3001
extern int VodAudioProcessorAudioMixingID;

@interface VodAudioProcessor : NSObject

/// Initialize
/// @param rtcKit ByteRTCEngineKit
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

