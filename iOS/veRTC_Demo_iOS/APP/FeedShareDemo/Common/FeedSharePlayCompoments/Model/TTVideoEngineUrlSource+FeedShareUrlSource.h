//
//  TTVideoEngineUrlSource+FeedShareUrlSource.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/4/14.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "TTVideoEngineUrlSource.h"

NS_ASSUME_NONNULL_BEGIN

@interface TTVideoEngineUrlSource (FeedShareUrlSource)

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *cover;

@end

NS_ASSUME_NONNULL_END
