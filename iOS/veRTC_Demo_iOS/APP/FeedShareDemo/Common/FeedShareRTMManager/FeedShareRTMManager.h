//
//  FeedShareControlComponents.h
//  veRTC_Demo
//
//  Created by on 2022/4/7.
//  
//

#import <Foundation/Foundation.h>
#import "FeedShareRoomModel.h"
#import "FeedShareVideoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareRTMManager : NSObject

+ (void)requestJoinRoomWithRoomID:(NSString *)roomID
                            block:(void(^)(FeedShareRoomModel *roomModel, RTMACKModel *model))block;

+ (void)requestVideoListWithRoomID:(NSString *)roomID
                             block:(void(^)(NSArray<FeedShareVideoModel *> *videoList, RTMACKModel *model))block;

+ (void)requestLeaveRoom:(NSString *)roomID
                   block:(void(^)(RTMACKModel *model))block;

+ (void)requestChangeRoomScene:(FeedShareRoomStatus)roomStatus
                        roomID:(NSString *)roomID
                         block:(void(^)(RTMACKModel *model))block;

/// Mutual kick notification
/// @param block Callback
+ (void)clearUser:(void (^)(RTMACKModel *model))block;

+ (void)reconnect:(void (^)(RTMACKModel *model))block;

#pragma mark - Notification Message

+ (void)onUserJoinWithBlock:(void(^)(NSString *roomID, NSString *userID, NSString *userName))block;

+ (void)onUserLeaveWithBlock:(void(^)(NSString *roomID, NSString *userID, NSString *userName))block;

+ (void)onFinishRoomWithBlock:(void(^)(NSString *roomID))block;

+ (void)onUpdateRoomSceneWithBlock:(void(^)(NSString *roomID, FeedShareRoomStatus roomStatus))block;

+ (void)onVideoListUpdateWithBlock:(void(^)(NSArray<FeedShareVideoModel*> *videoList))block;

@end

NS_ASSUME_NONNULL_END
