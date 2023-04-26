// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "FeedShareRTSManager.h"
#import "FeedShareRTCManager.h"
#import "JoinRTSParams.h"

@implementation FeedShareRTSManager

+ (void)requestJoinRoomWithRoomID:(NSString *)roomID
                            block:(void(^)(FeedShareRoomModel *roomModel, RTSACKModel *model))block {
    NSDictionary *dic = @{
        @"room_id" : roomID,
        @"user_name" : [LocalUserComponent userModel].name,
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twJoinRoom"
                                           with:dic
                                          block:^(RTSACKModel * _Nonnull ackModel) {
        FeedShareRoomModel *roomModel = nil;
        if ([FeedShareRTSManager ackModelResponseClass:ackModel]) {
            roomModel = [FeedShareRoomModel yy_modelWithJSON:ackModel.response];
        }
        if (block) {
            block(roomModel, ackModel);
        }
        NSLog(@"[%@]-twJoinRoom %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)requestVideoListWithRoomID:(NSString *)roomID
                             block:(void(^)(NSArray<FeedShareVideoModel *> *videoList, RTSACKModel *model))block {
    
    NSDictionary *dic = @{
        @"room_id" : roomID,
        @"dt" : [UIDevice currentDevice].model,
        @"device_brand" : @"Apple",
        @"os" : @"iOS",
        @"os_version" : [UIDevice currentDevice].systemVersion,
        @"client_version" : [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"],
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twGetContentList"
                                           with:dic
                                          block:^(RTSACKModel * _Nonnull ackModel) {
        
        NSArray<FeedShareVideoModel *> *videoList = nil;
        if ([FeedShareRTSManager ackModelResponseClass:ackModel]) {
            videoList = [NSArray yy_modelArrayWithClass:[FeedShareVideoModel class] json:ackModel.response[@"content_list"]];
        }
        if (block) {
            block(videoList, ackModel);
        }
        NSLog(@"[%@]-twGetContentList %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)requestLeaveRoom:(NSString *)roomID block:(nonnull void (^)(RTSACKModel * _Nonnull))block {
    NSDictionary *dic = @{
        @"room_id" : roomID
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twLeaveRoom"
                                           with:dic
                                          block:^(RTSACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-twLeaveRoom %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)requestChangeRoomScene:(FeedShareRoomStatus)roomStatus
                        roomID:(nonnull NSString *)roomID
                         block:(nonnull void (^)(RTSACKModel * _Nonnull))block {
    NSDictionary *dic = @{
        @"room_id" : roomID,
        @"room_scene" : @(roomStatus)
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twUpdateRoomScene"
                                           with:dic
                                          block:^(RTSACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-twUpdateRoomScene %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)clearUser:(void (^)(RTSACKModel *model))block {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twClearUser" with:dic block:^(RTSACKModel * _Nonnull ackModel) {
        
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-twClearUser %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)reconnect:(void (^)(RTSACKModel *model))block {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twReconnect" with:dic block:^(RTSACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
    }];
}

#pragma mark - Notification Message
+ (void)onUserJoinWithBlock:(void(^)(NSString *roomID, NSString *userID, NSString *userName))block {
    
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnJoinRoom"
                                              block:^(RTSNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *roomID = @"";
        NSString *userID = @"";
        NSString *userName = @"";
        
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"room_id"]];
            userID = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_id"]];
            userName = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_name"]];
        }
        if (block) {
            block(roomID, userID, userName);
        }
        NSLog(@"[%@]-twOnJoinRoom %@", [self class], noticeModel.data);
    }];
}

+ (void)onUserLeaveWithBlock:(void(^)(NSString *roomID, NSString *userID, NSString *userName))block {
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnLeaveRoom"
                                              block:^(RTSNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *roomID = @"";
        NSString *userID = @"";
        NSString *userName = @"";
        
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"room_id"]];
            userID = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_id"]];
            userName = [NSString stringWithFormat:@"%@", noticeModel.data[@"user_name"]];
        }
        if (block) {
            block(roomID, userID, userName);
        }
        NSLog(@"[%@]-twOnLeaveRoom %@", [self class], noticeModel.data);
    }];
}

+ (void)onFinishRoomWithBlock:(void(^)(NSString *roomID, NSString *type))block {
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnFinishRoom"
                                              block:^(RTSNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *roomID = @"";
        NSString *type = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"room_id"]];
            type = [NSString stringWithFormat:@"%@", noticeModel.data[@"type"]];
        }
        if (block) {
            block(roomID, type);
        }
        NSLog(@"[%@]-twOnFinishRoom %@", [self class], noticeModel.data);
    }];
}

+ (void)onUpdateRoomSceneWithBlock:(void(^)(NSString *roomID, FeedShareRoomStatus roomStatus))block {
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnUpdateRoomScene"
                                              block:^(RTSNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *roomID = @"";
        FeedShareRoomStatus roomStatus = FeedShareRoomStatusChat;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"room_id"]];
            roomStatus = [noticeModel.data[@"room_scene"] integerValue];
        }
        if (block) {
            block(roomID, roomStatus);
        }
        NSLog(@"[%@]-twOnUpdateRoomScene %@", [self class], noticeModel.data);
    }];
}

+ (void)onVideoListUpdateWithBlock:(void(^)(NSArray<FeedShareVideoModel*> *videoList))block {
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnContentUpdate"
                                              block:^(RTSNoticeModel * _Nonnull
                                                      noticeModel) {
        NSArray<FeedShareVideoModel*> *videoList = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            videoList = [NSArray yy_modelArrayWithClass:[FeedShareVideoModel class] json:noticeModel.data[@"content_list"]];
        }
        if (block) {
            block(videoList);
        }
        NSLog(@"[%@]-twOnContentUpdate %@", [self class], noticeModel.data);
    }];
}

#pragma mark - tool

+ (BOOL)ackModelResponseClass:(RTSACKModel *)ackModel {
    if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
        return YES;
    } else {
        return NO;
    }
}

@end
