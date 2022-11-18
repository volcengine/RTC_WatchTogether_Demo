//
//  FeedShareRTCManager.m
//  veRTC_Demo
//
//  Created by on 2022/1/5.
//  
//

#import "FeedShareRTCManager.h"
#import "SystemAuthority.h"
#import "FeedShareVideoConfig.h"
#import <WatchBase/VodAudioProcessor.h>

@interface FeedShareRTCManager ()<ByteRTCVideoDelegate>

@property (nonatomic, assign) int audioMixingID;
@property (nonatomic, assign) ByteRTCCameraID cameraID;
@property (nonatomic, strong) NSMutableDictionary<NSString *, UIView *> *streamViewDic;
@property (nonatomic, strong) NSMutableSet *stopVideoUserSet;
@property (nonatomic, copy) FeedShareNetworkQualityChangeBlock networkQualityBlock;
@property (nonatomic, strong) ByteRTCVideoEncoderConfig *solution;


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
    CGSize videoSize = [FeedShareVideoConfig defaultVideoSize];
    self.solution.width = videoSize.width;
    self.solution.height = videoSize.height;
    self.solution.frameRate = [FeedShareVideoConfig frameRate];
    self.solution.maxBitrate = [FeedShareVideoConfig maxKbps];
    [self.rtcEngineKit SetMaxVideoEncoderConfig:self.solution];
    
    //设置视频镜像
    [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];

    _cameraID = ByteRTCCameraIDFront;

    _audioMixingID = 3001;
    VodAudioProcessorAudioMixingID = _audioMixingID;
}

- (void)joinChannelWithToken:(NSString *)token roomID:(NSString *)roomID uid:(NSString *)uid {
    self.recordingVolume = 0.5;
    
    //设置音频路由模式，YES 扬声器/NO 听筒
    //Set the audio routing mode, YES speaker/NO earpiece
    [self.rtcEngineKit setDefaultAudioRoute:ByteRTCAudioRouteSpeakerphone];
    
    //开启/关闭发言者音量键控
    //Turn on/off speaker volume keying
    ByteRTCAudioPropertiesConfig *audioPropertiesConfig = [[ByteRTCAudioPropertiesConfig alloc] init];
    audioPropertiesConfig.interval = 300;
    [self.rtcEngineKit enableAudioPropertiesReport:audioPropertiesConfig];

    //加入房间，开始连麦,需要申请AppId和Token
    //Join the room, start connecting the microphone, you need to apply for AppId and Token
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = uid;
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileCommunication;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    config.isAutoSubscribeVideo = YES;
    self.rtcRoom = [self.rtcEngineKit createRTCRoom:roomID];
    self.rtcRoom.delegate = self;
    int result = [self.rtcRoom joinRoomByToken:token userInfo:userInfo roomConfig:config];

    NSLog(@"%d", result);
}

- (void)startAudioMixing {
    CGSize videoSize = [FeedShareVideoConfig watchingVideoSize];
    self.solution.width = videoSize.width;
    self.solution.height = videoSize.height;
    [self.rtcEngineKit SetMaxVideoEncoderConfig:self.solution];
    
    ByteRTCAudioMixingManager *manager = [self.rtcEngineKit getAudioMixingManager];
    [manager enableAudioMixingFrame:_audioMixingID type:ByteRTCAudioMixingTypePlayout];
    
    self.audioMixingVolume = 0.1;
}

- (void)stopAudioMixing {
    
    CGSize videoSize = [FeedShareVideoConfig defaultVideoSize];
    self.solution.width = videoSize.width;
    self.solution.height = videoSize.height;
    
    [self.rtcEngineKit SetMaxVideoEncoderConfig:self.solution];
    
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
                [self.rtcRoom publishStream:ByteRTCMediaStreamTypeAudio];
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
                [self.rtcRoom publishStream:ByteRTCMediaStreamTypeVideo];
                NSLog(@"Manager RTCSDK startVideoCapture");
            }
        }];
    } else {
        [self.rtcEngineKit stopVideoCapture];
        NSLog(@"Manager RTCSDK stopVideoCapture");
    }
    
    if (enable) {
        [self.stopVideoUserSet removeObject:[LocalUserComponent userModel].uid];
    } else {
        [self.stopVideoUserSet addObject:[LocalUserComponent userModel].uid];
    }
}

- (void)switchCamera {
    if (self.cameraID == ByteRTCCameraIDFront) {
        self.cameraID = ByteRTCCameraIDBack;
    } else {
        self.cameraID = ByteRTCCameraIDFront;
    }
    
    if (self.cameraID == ByteRTCCameraIDFront) {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    } else {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeNone];
    }
    
    [self.rtcEngineKit switchCamera:self.cameraID];
}

- (void)updateCameraID:(BOOL)isFront {
    self.cameraID = isFront ? ByteRTCCameraIDFront : ByteRTCCameraIDBack;
    
    if (self.cameraID == ByteRTCCameraIDFront) {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    } else {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeNone];
    }
    
    [self.rtcEngineKit switchCamera:self.cameraID];
}

- (void)startPreview:(UIView *_Nullable)view {
    ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
    canvas.view = view;
    canvas.renderMode = ByteRTCRenderModeHidden;
    canvas.view.backgroundColor = [UIColor clearColor];
    canvas.uid = self.rtcRoom.getRoomId;
    //设置本地视频显示信息
    //Set local video display information
    [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain withCanvas:canvas];
    [self.rtcEngineKit startVideoCapture];
}

- (void)muteLocalVideo:(BOOL)mute {
    if (mute) {
        [self.rtcRoom unpublishStream:ByteRTCMediaStreamTypeVideo];
    }else {
        [self.rtcRoom publishStream:ByteRTCMediaStreamTypeVideo];
    }
    NSLog(@"Manager RTCSDK muteLocalVideo");
}

- (void)muteLocalAudio:(BOOL)mute {
    //开启/关闭 本地音频采集
    //Turn on/off local audio capture
    if (mute) {
        [self.rtcRoom unpublishStream:ByteRTCMediaStreamTypeAudio];
    } else {
        [self.rtcRoom publishStream:ByteRTCMediaStreamTypeAudio];
    }
    NSLog(@"Manager RTCSDK muteLocalAudio");
}

- (void)leaveChannel {
    //离开频道
    //Leave the channel
    [self.rtcRoom leaveRoom];
    [self.streamViewDic removeAllObjects];
    NSLog(@"Manager RTCSDK leaveChannel");
    
}

#pragma mark - Render

- (UIView *)getStreamViewWithUid:(NSString *)uid {
    if (IsEmptyStr(uid)) {
        return nil;
    }
    UIView *view = self.streamViewDic[uid];
    return view;
}

- (void)bingCanvasViewToUid:(NSString *)uid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
        
        if ([uid isEqualToString:[LocalUserComponent userModel].uid]) {
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
                [self.streamViewDic setValue:streamView forKey:uid];
                
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
                canvas.roomId = self.rtcRoom.getRoomId;
                [self.rtcEngineKit setRemoteVideoCanvas:canvas.uid
                                        withIndex:ByteRTCStreamIndexMain
                                       withCanvas:canvas];
                [self.streamViewDic setValue:remoteRoomView forKey:uid];
                
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

- (void)updateStopVideoUserCanvas {
    
    for (NSString *uid in self.stopVideoUserSet) {
        if ([_streamViewDic.allKeys containsObject:uid]) {
            [_streamViewDic removeObjectForKey:uid];
            [self bingCanvasViewToUid:uid];
        }
    }
    [self.stopVideoUserSet removeAllObjects];
}

#pragma mark - NetworkQuality

- (void)didChangeNetworkQuality:(FeedShareNetworkQualityChangeBlock)block {
    self.networkQualityBlock = block;
}

#pragma mark - message
- (void)sendMessage:(NSDictionary *)message toUserID:(NSString *)userID {
    NSString *string = [self messageString:message];
    if (string.length > 0) {
        [self.rtcRoom sendUserMessage:userID message:string config:ByteRTCMessageConfigReliableOrdered];
    }
}

- (void)sendRoomMessage:(NSDictionary *)message {
    NSString *string = [self messageString:message];
    if (string.length > 0) {
        [self.rtcRoom sendRoomMessage:string];
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

#pragma mark - ByteRTCVideoDelegate

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserJoined:(ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    NSLog(@"Manager RTCSDK onUserJoined %@", userInfo.userId);
    [self bingCanvasViewToUid:userInfo.userId];
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserLeave:(NSString *)uid reason:(ByteRTCUserOfflineReason)reason {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.streamViewDic removeObjectForKey:uid];
        [self noticeUserChange];
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onUserStartVideoCapture:(NSString *)roomId uid:(NSString *)uid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self.stopVideoUserSet removeObject:uid];
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onUserStopVideoCapture:(NSString *)roomId uid:(NSString *)uid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        [self.stopVideoUserSet addObject:uid];
    });
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onLocalStreamStats:(ByteRTCLocalStreamStats *)stats {

    FeedShareNetworkQualityStatus liveStatus = FeedShareNetworkQualityStatusNone;
    if (stats.tx_quality == ByteRTCNetworkQualityExcellent ||
        stats.tx_quality == ByteRTCNetworkQualityGood) {
        liveStatus = FeedShareNetworkQualityStatusGood;
    } else {
        liveStatus = FeedShareNetworkQualityStatusBad;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.networkQualityBlock) {
            self.networkQualityBlock(liveStatus, [LocalUserComponent userModel].uid);
        }
    });
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserMessageReceived:(NSString *)uid message:(NSString *)message {
    
    if ([self isServerMessage:message uid:uid]) {
        [super rtcRoom:rtcRoom onUserMessageReceived:uid message:message];
    }
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomMessageReceived:(NSString *)uid message:(NSString *)message {
    if ([self isServerMessage:message uid:uid]) {
        [super rtcRoom:rtcRoom onRoomMessageReceived:uid message:message];
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

- (ByteRTCVideoEncoderConfig *)solution {
    if (!_solution) {
        _solution =[[ByteRTCVideoEncoderConfig alloc] init];
    }
    return _solution;
}

- (NSMutableSet *)stopVideoUserSet {
    if (!_stopVideoUserSet) {
        _stopVideoUserSet = [NSMutableSet set];
    }
    return _stopVideoUserSet;
}

@end
