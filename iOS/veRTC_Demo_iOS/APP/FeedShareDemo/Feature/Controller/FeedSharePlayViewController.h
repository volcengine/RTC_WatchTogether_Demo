//
//  FeedSharePlayViewController.h
//  veRTC_Demo
//
//  Created by on 2022/1/6.
//  
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
