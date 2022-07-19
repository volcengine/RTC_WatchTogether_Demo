//
//  FeedShareVideoConfig.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/2/15.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareVideoConfig : NSObject

+ (CGSize)defaultVideoSize;

+ (CGSize)watchingVideoSize;

+ (NSInteger)frameRate;

+ (NSInteger)maxKbps;

@end

NS_ASSUME_NONNULL_END
