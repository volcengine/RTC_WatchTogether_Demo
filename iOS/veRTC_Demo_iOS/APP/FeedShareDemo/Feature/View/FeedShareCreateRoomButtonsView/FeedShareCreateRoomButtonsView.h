//
//  FeedShareCreateRoomButtonsView.h
//  FeedShareDemo
//
//  Created by   on 2022/10/14.
//

#import <UIKit/UIKit.h>
#import "FeedShareBottomButtonsView.h"
@class FeedShareCreateRoomButtonsView;

NS_ASSUME_NONNULL_BEGIN

@protocol FeedShareCreateRoomButtonsViewDelegate <NSObject>

- (void)feedShareCreateRoomButtonsView:(FeedShareCreateRoomButtonsView *)view
                    didClickButtonType:(FeedShareButtonType)type;

@end

@interface FeedShareCreateRoomButtonsView : UIView

@property(nonatomic, weak) id<FeedShareCreateRoomButtonsViewDelegate> delegate;

@property(nonatomic, assign) BOOL enableAudio;
@property(nonatomic, assign) BOOL enableVideo;

@end

NS_ASSUME_NONNULL_END
