// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "FeedSharePlayerComponent.h"
#import "TTVideoEngineHeader.h"

#import "FeedShareToolComponent.h"
#import "TTVideoEngine+Audio.h"
#import "FeedShareRTCManager.h"
#import "FeedShareMessageComponent.h"
#import "FeedShareVodAudioManager.h"
#import <TTSDK/TTVideoEngineHeader.h>

@interface FeedSharePlayerComponent ()<TTVideoEngineDelegate>

@property (nonatomic, weak) UIView *videoView;
@property (nonatomic, strong) TTVideoEngine *videoEngine;
@property (nonatomic, copy) NSString *cacheKey;
@property (nonatomic, strong) FeedShareVodAudioManager *audioProcesser;

@end

@implementation FeedSharePlayerComponent
{
    EngineAudioWrapper wrapper;
}

- (void)dealloc {
    NSLog(@"FeedSharePlayerComponentDealloc-%@-%@", self.videoEngine, wrapper.context);
    if (self.videoEngine) {
        NSLog(@"videoEngineg");
    }
    [self stop];
    
    wrapper.context = NULL;
}


- (instancetype)initWithPlayerView:(UIView *)videoView {
    if (self = [super init]) {
        self.videoView = videoView;
        _audioProcesser = [[FeedShareVodAudioManager alloc] initWithRTCKit:[FeedShareRTCManager shareRtc].rtcEngineKit];
    }
    return self;
}

- (void)initPlayer {
    
    if (self.videoEngine == nil) {
        self.videoEngine = [[TTVideoEngine alloc] initWithOwnPlayer:YES];
    }
    
    self.videoEngine.looping = YES;
    self.videoEngine.delegate = self;
    
    [self setVideoEngineOptions:self.videoEngine];
    
    [self.videoView addSubview:self.videoEngine.playerView];
    
    [self.videoEngine setOptionForKey:VEKKeyPlayerAudioDevice_ENUM value:@(TTVideoEngineDeviceDummyAudio)];
    [self.videoEngine setOptionForKey:VEKKeyPlayerDummyAudioSleep_BOOL value:@(NO)];
    wrapper.open = openAudio;
    wrapper.process = processAudio;
    wrapper.close = closeAudio;
    wrapper.release = releaseAudio;
    
    wrapper.context = (__bridge void *)_audioProcesser;
    [self.videoEngine setAudioProcessor:&wrapper];
}

- (void)setVideoEngineOptions:(TTVideoEngine *)videoEngine {
    
    [videoEngine configResolution:TTVideoEngineResolutionTypeHD];
    
    /// hardware decode,  suggest open
    [videoEngine setOptionForKey:VEKKeyPlayerHardwareDecode_BOOL value:@(YES)];

    /// h265 option
    [videoEngine setOptionForKey:VEKKeyPlayerh265Enabled_BOOL value:@(YES)];
    
    /// render engine, suggest use TTVideoEngineRenderEngineMetal
    [videoEngine setOptionForKey:VEKKeyViewRenderEngine_ENUM value:@(TTVideoEngineRenderEngineMetal)];
    
    /// optimize seek time-consuming, suggest open
    [videoEngine setOptionForKey:VEKKeyPlayerPreferNearestSampleEnable value:@(YES)];
    
    [videoEngine setOptionForKey:VEKKeyProxyServerEnable_BOOL value:@(YES)];
    
    /// Can optimize video id to play the first frame
    [videoEngine setOptionForKey:VEKKeyModelCacheVideoInfoEnable_BOOL value:@(YES)];
    
    [videoEngine setOptionForKey:VEKKeyViewScaleMode_ENUM value:@(TTVideoEngineScalingModeAspectFill)];
}

#pragma mark - TTVideoEngineDelegate

- (void)videoEngine:(TTVideoEngine *)videoEngine playbackStateDidChanged:(TTVideoEnginePlaybackState)playbackState {
    
}

- (void)videoEngine:(TTVideoEngine *)videoEngine loadStateDidChanged:(TTVideoEngineLoadState)loadState {
    
}

- (void)videoEngineCloseAysncFinish:(nonnull TTVideoEngine *)videoEngine {
    NSLog(@"videoEngineCloseAysncFinish");
}

- (void)videoEngineDidFinish:(nonnull TTVideoEngine *)videoEngine error:(nullable NSError *)error {
    
}

- (void)videoEngineDidFinish:(nonnull TTVideoEngine *)videoEngine videoStatusException:(NSInteger)status {
    
}

- (void)videoEngineUserStopped:(nonnull TTVideoEngine *)videoEngine {
    
}

- (void)videoEngine:(TTVideoEngine *)videoEngine mdlKey:(NSString *)key hitCacheSze:(NSInteger)cacheSize {
    NSLog(@"TTVideoEngine-cache:%@-size:%ld", key, cacheSize);
}


#pragma mark - playerAudio

static void openAudio (void *context, int samplerate, int channels, int duration) {
    FeedShareVodAudioManager *process = (__bridge FeedShareVodAudioManager *)(context);
    [process openAudio:samplerate channels:channels];
}

static void processAudio (void *context, float **inouts, int samples, int64_t timestamp) {
    FeedShareVodAudioManager *process = (__bridge FeedShareVodAudioManager *)(context);

    [process processAudio:inouts samples:samples];
}

static void closeAudio (void *context) {
    
}

static void releaseAudio (void *context) {
    
}

#pragma mark - methods
- (void)setVideoModel:(FeedShareVideoModel *)videoModel {
    _videoModel = videoModel;
    self.cacheKey = [FeedShareToolComponent MD5ForLower32Bate:videoModel.videoUrl];
    [self initPlayer];
    [self.videoEngine setVideoEngineVideoSource:[FeedShareVideoModel videoEngineUrlSource:videoModel]];
}

- (void)play {
    NSLog(@"TTVideoEngine-play:%@", self.cacheKey);
    [self.videoEngine play];
}

- (void)pause {
    [self.videoEngine pause];
}

- (void)stop {
    [self.videoEngine stop];
    [self closeVideoPlayer];
}

- (void)closeVideoPlayer {
    [self.videoEngine.playerView removeFromSuperview];
    [self.videoEngine close];
    [self.videoEngine removeTimeObserver];
    self.videoEngine = nil;
}

- (void)setCurrentPlaybackTime:(NSTimeInterval)time complete:(void(^)(void))complete {
    [self.videoEngine setCurrentPlaybackTime:time complete:^(BOOL success) {
        NSLog(@"seek-%d", success);
        if (success) {
            if (complete) {
                complete();
            }
        }
        
    } renderComplete:^{
        NSLog(@"renderComplete");
        
    }];
}

- (NSTimeInterval)totalDuration {
    return self.videoEngine.duration;
}

- (NSTimeInterval)currentDuration {
    return self.videoEngine.currentPlaybackTime;
}

- (BOOL)isPlaying {
    return self.videoEngine.playbackState == TTVideoEnginePlaybackStatePlaying;
}

- (BOOL)isPaused {
    return self.videoEngine.playbackState == TTVideoEnginePlaybackStatePaused;
}

@end
