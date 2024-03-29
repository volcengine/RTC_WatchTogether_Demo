// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "FeedShareVideoModel.h"
#import "FeedSharePlayerComponent.h"
#import "FeedShareVideoMessageModel.h"
@class FeedShareMessageComponent;

NS_ASSUME_NONNULL_BEGIN

@protocol FeedShareMessageComponentDelegate <NSObject>

@optional
- (void)feedShareMessageComponent:(FeedShareMessageComponent *)messageComponent
     didReceivedVideoMessageModel:(FeedShareVideoMessageModel *)videoMessageModel;

- (void)feedShareMessageComponentDidReceiveRequestFeedShare:(FeedShareMessageComponent *)messageComponent;

@end

@interface FeedShareMessageComponent : NSObject

- (instancetype)initWithDelegate:(id<FeedShareMessageComponentDelegate>)delegate;

- (void)addMessageListener;

- (void)sendMessage:(FeedShareVideoMessageModel *)videoMessageModel;

- (void)sendRequestFeedShareMessage:(NSString *)toUserID;

@end

NS_ASSUME_NONNULL_END
