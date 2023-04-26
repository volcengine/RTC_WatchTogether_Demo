// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "TTVideoEngineUrlSource+FeedShareUrlSource.h"

@implementation TTVideoEngineUrlSource (FeedShareUrlSource)

#pragma mark - Setter && getter

- (void)setTitle:(NSString *)title {
    objc_setAssociatedObject(self, @selector(title), title, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSString *)title {
    return objc_getAssociatedObject(self, @selector(title));
}

- (void)setCover:(NSString *)cover {
    objc_setAssociatedObject(self, @selector(cover), cover, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSString *)cover {
    return objc_getAssociatedObject(self, @selector(cover));
}


@end
