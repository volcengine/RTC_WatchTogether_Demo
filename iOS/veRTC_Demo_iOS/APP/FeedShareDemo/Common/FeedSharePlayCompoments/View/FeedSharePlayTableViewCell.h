//
//  FeedSharePlayTableViewCell.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/6.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FeedSharePlayerComponent.h"
@class FeedShareVideoModel;

NS_ASSUME_NONNULL_BEGIN

@interface FeedSharePlayTableViewCell : UITableViewCell

@property (nonatomic, strong, readonly) FeedSharePlayerComponent *player;
@property (nonatomic, strong) FeedShareVideoModel *videoModel;

@property (nonatomic, copy) void(^videoStateChangedBlock)(BOOL isPause);

- (void)play;
- (void)stop;
- (void)pause;

@end

NS_ASSUME_NONNULL_END
