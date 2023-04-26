// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "FeedShareVideoModel.h"
#import "FeedShareRoomModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedSharePlayComponent : NSObject

@property (nonatomic, strong, readonly) UITableView *tableView;

- (instancetype)initWithRoomModel:(FeedShareRoomModel *)roomModel;

- (void)updateVideoList:(NSArray<FeedShareVideoModel*> *)videoList;

- (void)destroy;


@end

NS_ASSUME_NONNULL_END
