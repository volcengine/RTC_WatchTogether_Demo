// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
@class FeedShareRoomModel;
@class FeedShareVideoModel;

NS_ASSUME_NONNULL_BEGIN

@interface FeedSharePlayViewController : UIViewController

- (instancetype)initWithRoomModel:(FeedShareRoomModel *)roomModel;

- (void)updateVideoList:(NSArray<FeedShareVideoModel *> *)videoList;

- (void)popToCreateRoomViewController;
- (void)popToRoomViewController;

- (void)destroy;


@end

NS_ASSUME_NONNULL_END
