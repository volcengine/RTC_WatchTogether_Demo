// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "FeedSharePlayViewController.h"
#import "FeedShareRoomModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareRoomViewController : UIViewController

@property (nonatomic, weak) FeedSharePlayViewController *playController;
@property (nonatomic, copy) NSArray<FeedShareVideoModel *> * _Nullable videoModelArray;

- (instancetype)initWithRoomModel:(FeedShareRoomModel *)roomModel;

- (BOOL)isHost;

- (void)quitRoom;

- (void)receivedVideoList:(NSArray<FeedShareVideoModel *> *)videoList;

- (void)receiveUpdateRoomScene:(NSString *)roomID scene:(FeedShareRoomStatus)scene;

- (void)receiveFinishRoom:(NSString *)roomID type:(NSString *)type;

@end

NS_ASSUME_NONNULL_END
