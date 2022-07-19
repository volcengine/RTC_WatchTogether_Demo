//
//  FeedShareVideoMessageModel.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/4/11.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, FeedShareVideoStatus) {
    FeedShareVideoStatusPause = 1,
    FeedShareVideoStatusPlay = 2,
};

@interface FeedShareVideoMessageModel : NSObject

@property (nonatomic, copy) NSString *videoID;
@property (nonatomic, assign) NSTimeInterval totalDuration;
@property (nonatomic, assign) NSTimeInterval currentDuration;
@property (nonatomic, assign) FeedShareVideoStatus videoStatus;

@end

NS_ASSUME_NONNULL_END
