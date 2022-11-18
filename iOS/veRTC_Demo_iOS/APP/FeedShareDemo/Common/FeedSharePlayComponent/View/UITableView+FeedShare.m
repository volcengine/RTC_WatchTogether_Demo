//
//  UITableView+FeedShare.m
//  veRTC_Demo
//
//  Created by on 2022/4/11.
//  
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
