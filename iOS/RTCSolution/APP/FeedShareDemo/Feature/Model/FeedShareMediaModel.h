// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareMediaModel : NSObject

@property (nonatomic, assign) BOOL enableAudio;
@property (nonatomic, assign) BOOL enableVideo;
@property (nonatomic, assign) BOOL audioPermissionDenied;
@property (nonatomic, assign) BOOL videoPermissionDenied;

+ (instancetype)shared;

- (void)resetMediaStatus;

@end

NS_ASSUME_NONNULL_END
