// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "FeedShareVideoModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, FeedShareRoomStatus) {
    FeedShareRoomStatusChat = 1,
    FeedShareRoomStatusShare = 2,
};

@interface FeedShareRoomModel : NSObject

@property (nonatomic, copy) NSString *appID;
@property (nonatomic, copy) NSString *roomID;
@property (nonatomic, copy) NSString *hostUid;
@property (nonatomic, copy) NSString *hostName;
@property (nonatomic, assign) FeedShareRoomStatus roomStatus;
@property (nonatomic, copy) NSString *rtcToken;
@property (nonatomic, copy) NSArray<FeedShareVideoModel *> *videoList;


@end

NS_ASSUME_NONNULL_END
