//
//  FeedShareControlComponents.m
//  veRTC_Demo
//
//  Created by on 2022/4/7.
//  
//

#import "FeedShareRTMManager.h"
#import "FeedShareRTCManager.h"
#import "JoinRTSParams.h"

@implementation FeedShareRTMManager

+ (void)requestJoinRoomWithRoomID:(NSString *)roomID
                            block:(void(^)(FeedShareRoomModel *roomModel, RTMACKModel *model))block {
    NSDictionary *dic = @{
        @"room_id" : roomID,
        @"user_name" : [LocalUserComponent userModel].name,
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twJoinRoom"
                                           with:dic
                                          block:^(RTMACKModel * _Nonnull ackModel) {
        FeedShareRoomModel *roomModel = nil;
        if ([FeedShareRTMManager ackModelResponseClass:ackModel]) {
            roomModel = [FeedShareRoomModel yy_modelWithJSON:ackModel.response];
        }
        if (block) {
            block(roomModel, ackModel);
        }
        NSLog(@"[%@]-twJoinRoom %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)requestVideoListWithRoomID:(NSString *)roomID
                             block:(void(^)(NSArray<FeedShareVideoModel *> *videoList, RTMACKModel *model))block {
    
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
                                          block:^(RTMACKModel * _Nonnull ackModel) {
        
        NSArray<FeedShareVideoModel *> *videoList = nil;
        if ([FeedShareRTMManager ackModelResponseClass:ackModel]) {
            videoList = [NSArray yy_modelArrayWithClass:[FeedShareVideoModel class] json:ackModel.response[@"content_list"]];
        }
        if (block) {
            block(videoList, ackModel);
        }
        NSLog(@"[%@]-twGetContentList %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)requestLeaveRoom:(NSString *)roomID block:(nonnull void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{
        @"room_id" : roomID
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twLeaveRoom"
                                           with:dic
                                          block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-twLeaveRoom %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)requestChangeRoomScene:(FeedShareRoomStatus)roomStatus
                        roomID:(nonnull NSString *)roomID
                         block:(nonnull void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = @{
        @"room_id" : roomID,
        @"room_scene" : @(roomStatus)
    };
    dic = [JoinRTSParams addTokenToParams:dic];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twUpdateRoomScene"
                                           with:dic
                                          block:^(RTMACKModel * _Nonnull ackModel) {
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-twUpdateRoomScene %@ \n %@", [self class], dic, ackModel.response);
    }];
}

+ (void)clearUser:(void (^)(RTMACKModel *model))block {
    NSDictionary *dic = [JoinRTSParams addTokenToParams:nil];
    
    [[FeedShareRTCManager shareRtc] emitWithAck:@"twClearUser" with:dic block:^(RTMACKModel * _Nonnull ackModel) {
        
        if (block) {
            block(ackModel);
        }
        NSLog(@"[%@]-twClearUser %@ \n %@", [self class], dic, ackModel.response);
    }];
}

#pragma mark - Notification Message
+ (void)onUserJoinWithBlock:(void(^)(NSString *roomID, NSString *userID, NSString *userName))block {
    
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnJoinRoom"
                                              block:^(RTMNoticeModel * _Nonnull
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
                                              block:^(RTMNoticeModel * _Nonnull
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

+ (void)onFinishRoomWithBlock:(void(^)(NSString *roomID))block {
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnFinishRoom"
                                              block:^(RTMNoticeModel * _Nonnull
                                                      noticeModel) {
        NSString *roomID = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomID = [NSString stringWithFormat:@"%@", noticeModel.data[@"room_id"]];
        }
        if (block) {
            block(roomID);
        }
        NSLog(@"[%@]-twOnFinishRoom %@", [self class], noticeModel.data);
    }];
}

+ (void)onUpdateRoomSceneWithBlock:(void(^)(NSString *roomID, FeedShareRoomStatus roomStatus))block {
    [[FeedShareRTCManager shareRtc] onSceneListener:@"twOnUpdateRoomScene"
                                              block:^(RTMNoticeModel * _Nonnull
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
                                              block:^(RTMNoticeModel * _Nonnull
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

+ (BOOL)ackModelResponseClass:(RTMACKModel *)ackModel {
    if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
        return YES;
    } else {
        return NO;
    }
}

@end
