//
//  FeedShareVideoConfig.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/2/15.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedShareVideoConfig.h"

@implementation FeedShareVideoConfig

+ (CGSize)defaultVideoSize {
    return CGSizeMake(480, 640);
}

+ (CGSize)watchingVideoSize {
    return CGSizeMake(240, 240);
}

+ (NSInteger)frameRate {
    return 10;
}

+ (NSInteger)maxKbps {
    return 400;
}

@end
