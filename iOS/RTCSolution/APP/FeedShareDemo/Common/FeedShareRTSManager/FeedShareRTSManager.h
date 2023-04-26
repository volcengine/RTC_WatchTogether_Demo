// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "FeedShareRoomModel.h"
#import "FeedShareVideoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareRTSManager : NSObject

+ (void)requestJoinRoomWithRoomID:(NSString *)roomID
                            block:(void(^)(FeedShareRoomModel *roomModel, RTSACKModel *model))block;

+ (void)requestVideoListWithRoomID:(NSString *)roomID
                             block:(void(^)(NSArray<FeedShareVideoModel *> *videoList, RTSACKModel *model))block;

+ (void)requestLeaveRoom:(NSString *)roomID
                   block:(void(^)(RTSACKModel *model))block;

+ (void)requestChangeRoomScene:(FeedShareRoomStatus)roomStatus
                        roomID:(NSString *)roomID
                         block:(void(^)(RTSACKModel *model))block;

/// Mutual kick notification
/// @param block Callback
+ (void)clearUser:(void (^)(RTSACKModel *model))block;

+ (void)reconnect:(void (^)(RTSACKModel *model))block;

#pragma mark - Notification Message

+ (void)onUserJoinWithBlock:(void(^)(NSString *roomID, NSString *userID, NSString *userName))block;

+ (void)onUserLeaveWithBlock:(void(^)(NSString *roomID, NSString *userID, NSString *userName))block;

+ (void)onFinishRoomWithBlock:(void(^)(NSString *roomID, NSString *type))block;

+ (void)onUpdateRoomSceneWithBlock:(void(^)(NSString *roomID, FeedShareRoomStatus roomStatus))block;

+ (void)onVideoListUpdateWithBlock:(void(^)(NSArray<FeedShareVideoModel*> *videoList))block;

@end

NS_ASSUME_NONNULL_END
