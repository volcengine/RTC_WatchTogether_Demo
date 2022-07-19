//
//  FeedSharePlayViewController.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/6.
//  Copyright © 2022 bytedance. All rights reserved.
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


@end

NS_ASSUME_NONNULL_END
