//
//  FeedShareRoomModel.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/5.
//  Copyright © 2022 bytedance. All rights reserved.
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
@property (nonatomic, strong) NSArray<FeedShareVideoModel *> *videoList;


@end

NS_ASSUME_NONNULL_END
