//
//  FeedShareRoomViewController+SocketControl.m
//  veRTC_Demo
//
//  Created by on 2022/4/8.
//  
//

#import "FeedShareRoomViewController+SocketControl.h"
#import "FeedShareRTMManager.h"

@implementation FeedShareRoomViewController (SocketControl)

- (void)addSocketListener {
    __weak typeof(self) weakSelf = self;
    [FeedShareRTMManager onUserJoinWithBlock:^(NSString * _Nonnull roomID, NSString * _Nonnull userID, NSString * _Nonnull userName) {
        
    }];
    
    [FeedShareRTMManager onUserLeaveWithBlock:^(NSString * _Nonnull roomID, NSString * _Nonnull userID, NSString * _Nonnull userName) {
        
    }];
    
    [FeedShareRTMManager onFinishRoomWithBlock:^(NSString * _Nonnull roomID) {
        [weakSelf receiveFinishRoom:roomID];
    }];
    
    [FeedShareRTMManager onUpdateRoomSceneWithBlock:^(NSString * _Nonnull roomID, FeedShareRoomStatus roomStatus) {
        [weakSelf receiveUpdateRoomScene:roomID scene:roomStatus];
    }];
    
    [FeedShareRTMManager onVideoListUpdateWithBlock:^(NSArray<FeedShareVideoModel *> * _Nonnull videoList) {
        [weakSelf receivedVideoList:videoList];
    }];
}

@end
