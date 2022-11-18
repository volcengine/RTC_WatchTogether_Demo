//
//  FeedShareNetworkView.h
//  veRTC_Demo
//
//  Created by on 2022/1/14.
//  
//

#import <UIKit/UIKit.h>
#import "FeedShareRTCManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareNetworkView : UIView

- (void)updateNetworkQualityStstus:(FeedShareNetworkQualityStatus)status;

@end

NS_ASSUME_NONNULL_END
