//
//  FeedShareMediaModel.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/4/7.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedShareMediaModel.h"

@implementation FeedShareMediaModel

+ (instancetype)shared {
    static FeedShareMediaModel *model = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        model = [[FeedShareMediaModel alloc] init];
    });
    return model;
}

- (void)resetMediaStatus {
    self.enableAudio = YES;
    self.enableVideo = YES;
    self.audioPermissionDenied = NO;
    self.videoPermissionDenied = NO;
}

@end
