//
//  BaseButton.h
//  quickstart
//
//  Created by bytedance on 2021/3/24.
//  Copyright © 2021 . All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ButtonStatus) {
    ButtonStatusNone,
    ButtonStatusActive,
    ButtonStatusIng,
    ButtonStatusIllegal,
};

@interface BaseButton : UIButton

@property (nonatomic, assign) ButtonStatus status;

- (void)bingImage:(UIImage *)image status:(ButtonStatus)status;

@end

NS_ASSUME_NONNULL_END
