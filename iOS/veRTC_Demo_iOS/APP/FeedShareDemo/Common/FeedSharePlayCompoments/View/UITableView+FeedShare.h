//
//  UITableView+FeedShare.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/4/11.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UITableView (FeedShare)

- (NSIndexPath *)currentIndexPathForFullScreenCell;

@end

NS_ASSUME_NONNULL_END
