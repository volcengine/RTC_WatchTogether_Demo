//
//  FeedShareRTCManager.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/5.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedShareRTCManager.h"
#import "SystemAuthority.h"
#import "FeedShareVideoConfig.h"
#import <WatchBase/VodAudioProcessor.h>

@interface FeedShareRTCManager ()<ByteRTCEngineDelegate>

@property (nonatomic, assign) int audioMixingID;
@property (nonatomic, assign) ByteRTCCameraID cameraID;
@property (nonatomic, strong) NSMutableDictionary<NSString *, UIView *> *streamViewDic;
@property (nonatomic, copy) FeedShareNetworkQualityChangeBlock networkQualityBlock;
@property (nonatomic, strong) ByteRTCVideoSolution *solution;


@end

@implementation FeedShareRTCManager


+ (FeedShareRTCManager *_Nullable)shareRtc {
    static FeedShareRTCManager *rtcManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        rtcManager = [[FeedShareRTCManager alloc] init];
    });
    return rtcManager;
}

#pragma mark - Publish Action

- (void)configeRTCEngine {
        
    // Encoder config
    self.solution.videoSize = [FeedShareVideoConfig defaultVideoSize];
    self.solution.frameRate = [FeedShareVideoConfig frameRate];
    self.solution.maxKbps = [FeedShareVideoConfig maxKbps];
    [self.rtcEngineKit setVideoEncoderConfig:@[ self.solution ]];
    
    _cameraID = ByteRTCCameraIDFront;

    _audioMixingID = 3001;
    VoidAudioMixingID = _audioMixingID;
}

- (void)joinChannelWithToken:(NSString *)token roomID:(NSString *)roomID uid:(NSString *)uid {
    //设置音频路由模式，YES 扬声器/NO 听筒
    //Set the audio routing mode, YES speaker/NO earpiece
    [self.rtcEngineKit setAudioPlaybackDevice:ByteRTCAudioPlaybackDeviceSpeakerphone];
    
    //开启/关闭发言者音量键控
    //Turn on/off speaker volume keying
    [self.rtcEngineKit setAudioVolumeIndicationInterval:300];
    
    self.recordingVolume = 0.5;
    self.audioMixingVolume = 0.1;

    //加入房间，开始连麦,需要申请AppId和Token
    //Join the room, start connecting the microphone, you need to apply for AppId and Token
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = uid;
    
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileLiveBroadcasting;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    config.isAutoSubscribeVideo = YES;
    
    int result = [self.rtcEngineKit joinRoomByKey:token
                        roomId:roomID
                      userInfo:userInfo
                 rtcRoomConfig:config];
    NSLog(@"%d", result);
}

- (void)startAudioMixing {
    self.solution.videoSize = [FeedShareVideoConfig watchingVideoSize];
    [self.rtcEngineKit setVideoEncoderConfig:@[ self.solution ]];
    
    ByteRTCAudioMixingManager *manager = [self.rtcEngineKit getAudioMixingManager];
    [manager enableAudioMixingFrame:_audioMixingID type:ByteRTCAudioMixingTypePlayout];
}

- (void)stopAudioMixing {
    self.solution.videoSize = [FeedShareVideoConfig defaultVideoSize];
    [self.rtcEngineKit setVideoEncoderConfig:@[ self.solution ]];
    
    ByteRTCAudioMixingManager *manager = [self.rtcEngineKit getAudioMixingManager];
    [manager disableAudioMixingFrame:_audioMixingID];
}

#pragma mark - rtc method

- (void)enableLocalAudio:(BOOL)enable {
    //开启/关闭 本地音频采集
    //Turn on/off local audio capture
    if (enable) {
        [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio
                                               block:^(BOOL isAuthorize) {
            if (isAuthorize) {
                [self.rtcEngineKit startAudioCapture];
                [self.rtcEngineKit muteLocalAudio:ByteRTCMuteStateOff];
                NSLog(@"Manager RTCSDK startAudioCapture");
            }
        }];
    } else {
        [self.rtcEngineKit stopAudioCapture];
        NSLog(@"Manager RTCSDK stopAudioCapture");
    }
}

- (void)enableLocalVideo:(BOOL)enable {
    if (enable) {
        [SystemAuthority authorizationStatusWithType:AuthorizationTypeCamera
                                               block:^(BOOL isAuthorize) {
            if (isAuthorize) {
                [self.rtcEngineKit startVideoCapture];
                [self.rtcEngineKit muteLocalVideo:ByteRTCMuteStateOff];
                NSLog(@"Manager RTCSDK startVideoCapture");
            }
        }];
    } else {
        [self.rtcEngineKit stopVideoCapture];
        NSLog(@"Manager RTCSDK stopAudioCapture");
    }
}

- (void)switchCamera {
    if (self.cameraID == ByteRTCCameraIDFront) {
        self.cameraID = ByteRTCCameraIDBack;
    } else {
        self.cameraID = ByteRTCCameraIDFront;
    }
    [self.rtcEngineKit switchCamera:self.cameraID];
}

- (void)updateCameraID:(BOOL)isFront {
    self.cameraID = isFront ? ByteRTCCameraIDFront : ByteRTCCameraIDBack;
    [self.rtcEngineKit switchCamera:self.cameraID];
}

- (void)startPreview:(UIView *_Nullable)view {
    ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
    canvas.view = view;
    canvas.renderMode = ByteRTCRenderModeHidden;
    canvas.view.backgroundColor = [UIColor clearColor];
    //设置本地视频显示信息
    //Set local video display information
    [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain withCanvas:canvas];
    [self.rtcEngineKit startVideoCapture];
}

- (void)muteLocalVideo:(BOOL)mute {
    [self.rtcEngineKit muteLocalVideo:mute];
    NSLog(@"Manager RTCSDK muteLocalVideo");
}

- (void)muteLocalAudio:(BOOL)mute {
    //开启/关闭 本地音频采集
    //Turn on/off local audio capture
    [self.rtcEngineKit muteLocalAudio:mute];
    NSLog(@"Manager RTCSDK muteLocalAudio");
}

- (void)leaveChannel {
    //离开频道
    //Leave the channel
    [self.rtcEngineKit leaveRoom];
    [self.streamViewDic removeAllObjects];
    NSLog(@"Manager RTCSDK leaveChannel");
    
}

#pragma mark - Render

- (UIView *)getStreamViewWithUid:(NSString *)uid {
    if (IsEmptyStr(uid)) {
        return nil;
    }
    NSString *typeStr = @"";
    if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
        typeStr = @"self";
    } else {
        typeStr = @"remote";
    }
    NSString *key = [NSString stringWithFormat:@"%@_%@", typeStr, uid];
    UIView *view = self.streamViewDic[key];
    return view;
}

- (void)bingCanvasViewToUid:(NSString *)uid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
        
        if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
            UIView *view = [self getStreamViewWithUid:uid];
            if (!view) {
                
                UIView *streamView = [[UIView alloc] init];
                streamView.backgroundColor = [UIColor grayColor];
                ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
                canvas.uid = uid;
                canvas.renderMode = ByteRTCRenderModeHidden;
                canvas.view.backgroundColor = [UIColor clearColor];
                canvas.view = streamView;
                [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain
                                      withCanvas:canvas];
                NSString *key = [NSString stringWithFormat:@"self_%@", uid];
                [self.streamViewDic setValue:streamView forKey:key];
                
                [self noticeUserChange];
            }
        } else {
            UIView *remoteRoomView = [self getStreamViewWithUid:uid];
            if (!remoteRoomView) {
                
                remoteRoomView = [[UIView alloc] init];
                remoteRoomView.backgroundColor = [UIColor grayColor];
                ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
                canvas.uid = uid;
                canvas.renderMode = ByteRTCRenderModeHidden;
                canvas.view.backgroundColor = [UIColor clearColor];
                canvas.view = remoteRoomView;
                [self.rtcEngineKit setRemoteVideoCanvas:canvas.uid
                                        withIndex:ByteRTCStreamIndexMain
                                       withCanvas:canvas];
                
                NSString *groupKey = [NSString stringWithFormat:@"remote_%@", uid];
                [self.streamViewDic setValue:remoteRoomView forKey:groupKey];
                
                [self noticeUserChange];
            }
        }
        NSLog(@"Manager RTCSDK bingCanvasViewToUid : %@", self.streamViewDic);
    }));
}

- (void)noticeUserChange {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.roomUsersDidChangeBlock) {
            self.roomUsersDidChangeBlock();
        }
    });
}

#pragma mark - NetworkQuality

- (void)didChangeNetworkQuality:(FeedShareNetworkQualityChangeBlock)block {
    self.networkQualityBlock = block;
}

#pragma mark - message
- (void)sendMessage:(NSDictionary *)message toUserID:(NSString *)userID {
    NSString *string = [self messageString:message];
    if (string.length > 0) {
        [self.rtcEngineKit sendUserMessage:userID message:string];
    }
}

- (void)sendRoomMessage:(NSDictionary *)message {
    NSString *string = [self messageString:message];
    if (string.length > 0) {
        [self.rtcEngineKit sendRoomMessage:string];
    }
}

- (NSString *)messageString:(NSDictionary *)message {
    NSMutableDictionary *dict = message.mutableCopy;
    [dict setValue:[NSUUID UUID].UUIDString forKey:@"message_id"];
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
    NSString *string = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    return string;
}

#pragma mark - ByteRTCEngineDelegate

- (void)rtcEngine:(ByteRTCEngineKit *)engine onRoomStateChanged:(NSString *)roomId withUid:(NSString *)uid state:(NSInteger)state extraInfo:(NSString *)extraInfo {
    [super rtcEngine:engine onRoomStateChanged:roomId withUid:uid state:state extraInfo:extraInfo];
    [self bingCanvasViewToUid:uid];
    NSLog(@"Manager RTCSDK join %@|%ld", uid, state);
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onUserJoined:(nonnull ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    NSLog(@"Manager RTCSDK onUserJoined %@", userInfo.userId);
    [self bingCanvasViewToUid:userInfo.userId];
    
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onUserLeave:(NSString *_Nonnull)uid reason:(ByteRTCUserOfflineReason)reason {
    
    NSString *groupKey = [NSString stringWithFormat:@"remote_%@", uid];
    [self.streamViewDic removeObjectForKey:groupKey];
    [self noticeUserChange];
}

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onLocalStreamStats:(const ByteRTCLocalStreamStats *_Nonnull)stats {
    FeedShareNetworkQualityStatus liveStatus = FeedShareNetworkQualityStatusNone;
    if (stats.tx_quality == ByteRTCNetworkQualityExcellent ||
        stats.tx_quality == ByteRTCNetworkQualityGood) {
        liveStatus = FeedShareNetworkQualityStatusGood;
    } else {
        liveStatus = FeedShareNetworkQualityStatusBad;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.networkQualityBlock) {
            self.networkQualityBlock(liveStatus, [LocalUserComponents userModel].uid);
        }
    });
}

- (void)rtcEngine:(ByteRTCEngineKit * _Nonnull)engine onUserMessageReceived:(NSString * _Nonnull)uid message:(NSString * _Nonnull)message {
    
    if ([self isServerMessage:message uid:uid]) {
        [super rtcEngine:engine onRoomMessageReceived:uid message:message];
    }
}

- (void)rtcEngine:(ByteRTCEngineKit *)engine onRoomMessageReceived:(NSString * _Nonnull)uid message:(NSString * _Nonnull)message {
    if ([self isServerMessage:message uid:uid]) {
        [super rtcEngine:engine onRoomMessageReceived:uid message:message];
    }
}

- (BOOL)isServerMessage:(NSString *)message uid:(NSString *)uid {
    if ([uid isEqualToString:@"server"]) {
        return YES;
    }
    else {
        NSData *data = [message dataUsingEncoding:NSUTF8StringEncoding];
        NSError *error = nil;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
        if (dict && self.receiveMessageBlock) {
            self.receiveMessageBlock(uid, dict);
        }
        return NO;
    }
}

#pragma mark - Background Music Method

- (void)setRecordingVolume:(CGFloat)recordingVolume {
    _recordingVolume = recordingVolume;
    [self.rtcEngineKit setPlaybackVolume:(int)(recordingVolume*200)];
}

- (void)setAudioMixingVolume:(CGFloat)audioMixingVolume {
    _audioMixingVolume = audioMixingVolume;
    ByteRTCAudioMixingManager *audioMixingManager = [self.rtcEngineKit getAudioMixingManager];
    [audioMixingManager setAudioMixingVolume:_audioMixingID volume:(int)(_audioMixingVolume*100) type:ByteRTCAudioMixingTypePlayout];
    NSLog(@"setAudioMixingVolume-%d", (int)(_audioMixingVolume*100));
}

#pragma mark - Getter

- (NSMutableDictionary<NSString *, UIView *> *)streamViewDic {
    if (!_streamViewDic) {
        _streamViewDic = [[NSMutableDictionary alloc] init];
    }
    return _streamViewDic;
}

- (ByteRTCVideoSolution *)solution {
    if (!_solution) {
        _solution = [[ByteRTCVideoSolution alloc] init];
    }
    return _solution;
}

@end
