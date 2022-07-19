//
//  FeedShareMessageComponment.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/9.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedShareMessageComponent.h"
#import "FeedShareRTCManager.h"

typedef NS_ENUM(NSInteger, FeedShareMessageType) {
    /// 视频播放状态
    FeedShareMessageTypeVideoStatus = 1,
    /// 观众求情一起看
    FeedShareMessageTypeRequestFeedShare = 2,
};

static NSString *const kMessageType = @"message_type";
static NSString *const kContent = @"content";

static NSString *const kVideoProcess = @"video_status";
static NSString *const kVideoID = @"video_id";
static NSString *const kVideoDuration = @"duration";
static NSString *const kVideoCurrentDuration = @"progress";
static NSString *const kVideoStatus = @"status";

@interface FeedShareMessageComponent ()

@property (nonatomic, weak) id<FeedShareMessageComponentDelegate> delegate;

@end

@implementation FeedShareMessageComponent

- (instancetype)initWithDelegate:(id<FeedShareMessageComponentDelegate>)delegate {
    if (self = [super init]) {
        self.delegate = delegate;
        __weak typeof(self) weakSelf = self;
        [FeedShareRTCManager shareRtc].receiveMessageBlock = ^(NSString * _Nonnull userID, NSDictionary * _Nonnull message) {
            [weakSelf receiveMessage:userID message:message];
        };
    }
    return self;
}

- (void)addMessageListener {
    __weak typeof(self) weakSelf = self;
    [FeedShareRTCManager shareRtc].receiveMessageBlock = ^(NSString * _Nonnull userID, NSDictionary * _Nonnull message) {
        [weakSelf receiveMessage:userID message:message];
    };
}

- (void)sendMessage:(FeedShareVideoMessageModel *)videoMessageModel {
    
    NSDictionary *message = @{
        kMessageType : @(FeedShareMessageTypeVideoStatus),
        kContent : @{
            kVideoProcess : @{
                kVideoID : videoMessageModel.videoID,
                kVideoDuration : @(videoMessageModel.totalDuration),
                kVideoCurrentDuration : @(videoMessageModel.currentDuration),
                kVideoStatus : @(videoMessageModel.videoStatus),
            }
        }
    };
    [[FeedShareRTCManager shareRtc] sendRoomMessage:message];
}

- (void)sendRequestFeedShareMessage:(NSString *)toUserID {
    NSDictionary *message = @{
        kMessageType : @(FeedShareMessageTypeRequestFeedShare),
    };
    [[FeedShareRTCManager shareRtc] sendMessage:message toUserID:toUserID];
}

#pragma mark - message
- (void)receiveMessage:(NSString *)userID message:(NSDictionary *)message {
    if ([userID isEqualToString:[LocalUserComponents userModel].uid]) {
        return;
    }
    
    NSDictionary *content = message[kContent];
    FeedShareMessageType type = [message[kMessageType] integerValue];
    
    switch (type) {
        case FeedShareMessageTypeVideoStatus: {
            NSDictionary *dict = content[kVideoProcess];
            FeedShareVideoMessageModel *videoMessageModel = [[FeedShareVideoMessageModel alloc] init];
            videoMessageModel.videoID = dict[kVideoID];
            videoMessageModel.totalDuration = [dict[kVideoDuration] doubleValue];
            videoMessageModel.currentDuration = [dict[kVideoCurrentDuration] doubleValue];
            videoMessageModel.videoStatus = [dict[kVideoStatus] integerValue];
            __weak typeof(self) weakSelf = self;
            dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
                if ([weakSelf.delegate respondsToSelector:@selector(feedShareMessageComponent:didReceivedVideoMessageModel:)]) {
                    [weakSelf.delegate feedShareMessageComponent:weakSelf didReceivedVideoMessageModel:videoMessageModel];
                }
            });
        }
            break;
        case FeedShareMessageTypeRequestFeedShare: {
            __weak typeof(self) weakSelf = self;
            dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
                if ([weakSelf.delegate respondsToSelector:@selector(feedShareMessageComponentDidReceiveRequestFeedShare:)]) {
                    [weakSelf.delegate feedShareMessageComponentDidReceiveRequestFeedShare:weakSelf];
                }
            });
        }
            break;
        default:
            break;
    }
}

@end
