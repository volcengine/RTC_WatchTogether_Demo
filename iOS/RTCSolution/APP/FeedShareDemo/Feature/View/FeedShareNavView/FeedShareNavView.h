// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "FeedShareNetworkView.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareNavView : UIView

@property (nonatomic, strong) FeedShareNetworkView *networkView;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) void(^leaveButtonTouchBlock)(void);

@end

NS_ASSUME_NONNULL_END
