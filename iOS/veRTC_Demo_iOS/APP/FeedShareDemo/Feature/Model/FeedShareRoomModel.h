//
//  FeedShareRoomModel.h
//  veRTC_Demo
//
//  Created by on 2022/1/5.
//  
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
