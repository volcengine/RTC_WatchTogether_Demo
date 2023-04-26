// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "BaseRTCManager.h"
@class FeedShareRTCManager;
NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, FeedShareNetworkQualityStatus) {
    FeedShareNetworkQualityStatusNone,
    FeedShareNetworkQualityStatusGood,
    FeedShareNetworkQualityStatusBad,
};
typedef void(^FeedShareNetworkQualityChangeBlock)(FeedShareNetworkQualityStatus status, NSString *uid);

@protocol FeedShareRTCManagerDelegate <NSObject>

/**
 * @brief 房间状态改变时的回调。 通过此回调，您会收到与房间相关的警告、错误和事件的通知。 例如，用户加入房间，用户被移出房间等。
 * @param manager GameRTCManager 模型
 * @param joinModel RTCJoinModel模型房间信息、加入成功失败等信息。
 */
- (void)feedShareRTCManager:(FeedShareRTCManager *)manager
         onRoomStateChanged:(RTCJoinModel *)joinModel;

@end

@interface FeedShareRTCManager : BaseRTCManager

@property (nonatomic, weak) id<FeedShareRTCManagerDelegate> _Nullable delegate;

@property (nonatomic, strong, readonly) NSMutableDictionary<NSString *, UIView *> *streamViewDic;

@property (nonatomic, copy) void(^roomUsersDidChangeBlock)(void);

@property (nonatomic, copy) void(^receiveMessageBlock)(NSString *userID, NSDictionary *message);

/*
 * RTC Manager Singletons
 */
+ (FeedShareRTCManager *_Nullable)shareRtc;

#pragma mark - Base Method

/**
 * Join room
 * @param token token
 * @param roomID roomID
 * @param uid uid
 */
- (void)joinChannelWithToken:(NSString *)token roomID:(NSString *)roomID uid:(NSString *)uid;

/*
 * Switch local audio capture
 * @param enable ture:Turn on audio capture false：Turn off audio capture
 */
- (void)enableLocalAudio:(BOOL)enable;

/*
 * Switch local video capture
 * @param enable ture:Turn on audio capture false：Turn off video capture
 */
- (void)enableLocalVideo:(BOOL)enable;

/*
 * Switch the camera
 */
- (void)switchCamera;

/*
 * Set the camera is front
 */
- (void)updateCameraID:(BOOL)isFront;

/*
 * Enable preview
 */
- (void)startPreview:(UIView *_Nullable)view;

/*
 * Switch local audio push stream
 * @param mute ture:Turn on audio push stream false：Turn off audio push stream
 */
- (void)muteLocalAudio:(BOOL)mute;

/*
 * Switch local video push stream
 * @param mute ture:Turn on video push stream false：Turn off video push stream
 */
- (void)muteLocalVideo:(BOOL)mute;

/*
 * Leave the room
 */
- (void)leaveChannel;

#pragma mark - Background Music Method

/// [0, 1.0]
@property (nonatomic, assign) CGFloat recordingVolume;

/// [0, 1.0]
@property (nonatomic, assign) CGFloat audioMixingVolume;

/*
 * Enable audio mixing
 */
- (void)startAudioMixing;

/*
 * Turn off audio mixing
 */
- (void)stopAudioMixing;

#pragma mark - Render

/*
 * Get stream render view
 */
- (UIView *)getStreamViewWithUid:(NSString *)uid;

/*
 * Bind the stream to render the view
 */
- (void)bingCanvasViewToUid:(NSString *)uid;

/*
 * Update close video user canvas
 */
- (void)updateStopVideoUserCanvas;

#pragma mark - NetworkQuality

/*
 * Monitor network changes
 */
- (void)didChangeNetworkQuality:(FeedShareNetworkQualityChangeBlock)block;

#pragma mark - message

/*
 * Send user message
 */
- (void)sendMessage:(NSDictionary *)message toUserID:(NSString *)userID;

/*
 * Send room message
 */
- (void)sendRoomMessage:(NSDictionary *)message;


@end

NS_ASSUME_NONNULL_END
