//
//  FeedSharePlayCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/6.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "FeedShareVideoModel.h"
#import "FeedShareRoomModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedSharePlayCompoments : NSObject

@property (nonatomic, strong, readonly) UITableView *tableView;

- (instancetype)initWithRoomModel:(FeedShareRoomModel *)roomModel;

- (void)updateVideoList:(NSArray<FeedShareVideoModel*> *)videoList;

- (void)destroy;


@end

NS_ASSUME_NONNULL_END
