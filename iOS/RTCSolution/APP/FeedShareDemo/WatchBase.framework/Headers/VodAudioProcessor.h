//
//  VideoAudioProcesser.h
//  ByteRTC
//
//  Created by on 2022/2/10.
//

#import <Foundation/Foundation.h>
@class ByteRTCEngineKit;

/// Audio mixingID
extern int VoidAudioMixingID;

@interface VodAudioProcessor : NSObject

/// Initialize
/// @param rtcKit ByteRTCEngineKit
- (instancetype)initWithRTCKit:(ByteRTCEngineKit *)rtcKit;

/// Open audio
/// @param samplerate Samplerate
/// @param channels Channels
- (void)openAudio:(int)samplerate channels:(int)channels;

/// Process audio
/// @param inouts Inouts
/// @param samples Samples
- (void)processAudio:(float **)inouts samples:(int)samples;


@end

