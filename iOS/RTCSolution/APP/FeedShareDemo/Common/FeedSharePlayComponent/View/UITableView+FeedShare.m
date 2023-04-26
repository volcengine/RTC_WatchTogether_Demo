// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "UITableView+FeedShare.h"

@implementation UITableView (FeedShare)

- (NSIndexPath *)currentIndexPathForFullScreenCell {
    CGRect visibleRect = CGRectZero;
    visibleRect.origin = self.contentOffset;
    visibleRect.size = self.frame.size;

    CGPoint visiblePoint = CGPointMake(CGRectGetMidX(visibleRect),
                                       CGRectGetMidY(visibleRect));

    return [self indexPathForRowAtPoint:visiblePoint];
}

@end
