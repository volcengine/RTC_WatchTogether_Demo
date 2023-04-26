// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "FeedShareRoomViewController+SocketControl.h"
#import "FeedShareRTSManager.h"

@implementation FeedShareRoomViewController (SocketControl)

- (void)addSocketListener {
    __weak typeof(self) weakSelf = self;
    [FeedShareRTSManager onUserJoinWithBlock:^(NSString * _Nonnull roomID, NSString * _Nonnull userID, NSString * _Nonnull userName) {
        
    }];
    
    [FeedShareRTSManager onUserLeaveWithBlock:^(NSString * _Nonnull roomID, NSString * _Nonnull userID, NSString * _Nonnull userName) {
        
    }];
    
    [FeedShareRTSManager onFinishRoomWithBlock:^(NSString * _Nonnull roomID, NSString * _Nonnull type) {
        [weakSelf receiveFinishRoom:roomID type:type];
    }];
    
    [FeedShareRTSManager onUpdateRoomSceneWithBlock:^(NSString * _Nonnull roomID, FeedShareRoomStatus roomStatus) {
        [weakSelf receiveUpdateRoomScene:roomID scene:roomStatus];
    }];
    
    [FeedShareRTSManager onVideoListUpdateWithBlock:^(NSArray<FeedShareVideoModel *> * _Nonnull videoList) {
        [weakSelf receivedVideoList:videoList];
    }];
}

@end
