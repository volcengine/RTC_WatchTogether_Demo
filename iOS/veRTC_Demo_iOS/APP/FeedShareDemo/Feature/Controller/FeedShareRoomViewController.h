//
//  FeedShareChatRoomViewController.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/5.
//  Copyright © 2022 bytedance. All rights reserved.
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

- (void)receiveFinishRoom:(NSString *)roomID;

@end

NS_ASSUME_NONNULL_END
