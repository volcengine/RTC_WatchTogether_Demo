// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
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
@property (nonatomic, assign) BOOL isLoading;

@end

NS_ASSUME_NONNULL_END
