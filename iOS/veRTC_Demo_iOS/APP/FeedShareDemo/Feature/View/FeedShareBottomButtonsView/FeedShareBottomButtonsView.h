//
//  FeedShareBottomButtonsView.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/5.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import <UIKit/UIKit.h>
@class FeedShareBottomButtonsView;

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, FeedShareButtonType) {
    FeedShareButtonTypeAudio,
    FeedShareButtonTypeVideo,
    FeedShareButtonTypeBeauty,
    FeedShareButtonTypeWatch,
    FeedShareButtonTypeSetting,
};

typedef NS_ENUM(NSInteger, FeedShareButtonViewType) {
    FeedShareButtonViewTypePreView,
    FeedShareButtonViewTypeRoom,
    FeedShareButtonViewTypeWatch,
};

@protocol FeedShareBottomButtonsViewDelegate <NSObject>

- (void)feedShareBottomButtonsView:(FeedShareBottomButtonsView *)view didClickButtonType:(FeedShareButtonType)type;

@end

@interface FeedShareBottomButtonsView : UIView

@property (nonatomic, assign) FeedShareButtonViewType type;
@property (nonatomic, weak) id<FeedShareBottomButtonsViewDelegate> delegate;

@property (nonatomic, assign) BOOL enableAudio;
@property (nonatomic, assign) BOOL enableVideo;

@end

NS_ASSUME_NONNULL_END
