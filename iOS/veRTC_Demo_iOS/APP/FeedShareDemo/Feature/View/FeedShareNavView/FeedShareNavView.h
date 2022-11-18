//
//  FeedShareNavView.h
//  veRTC_Demo
//
//  Created by on 2022/1/6.
//  
//

#import <UIKit/UIKit.h>
#import "FeedShareNetworkView.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareNavView : UIView

@property (nonatomic, strong) FeedShareNetworkView *networkView;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) void(^leaveButtonTouchBlock)(void);

@end

NS_ASSUME_NONNULL_END
