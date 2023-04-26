// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
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
