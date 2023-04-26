// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "FeedSharePlaySessionView.h"
#import "FeedShareRTCManager.h"

@interface FeedSharePlaySessionView ()

@property (nonatomic, strong) UIStackView *stackView;
@property (nonatomic, strong) NSMutableArray<UIView *> *viewArray;

@end

@implementation FeedSharePlaySessionView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self addSubview:self.stackView];
    [self.stackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
    for (int i = 0; i < 3; i++) {
        UIView *view = [[UIView alloc] init];
        view.hidden = YES;
        [self.stackView addArrangedSubview:view];
        [self.viewArray addObject:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(90, 90));
        }];
    }
}

- (void)updateVideoViews {
    NSDictionary *dict = [FeedShareRTCManager shareRtc].streamViewDic.copy;
    NSArray *userIDs = dict.allKeys;
    
    for (int i = 0; i < 3; i++) {
        UIView *view = self.viewArray[i];
        if (userIDs.count > i) {
            view.hidden = NO;
            UIView *videoView = [dict objectForKey:userIDs[i]];
            videoView.backgroundColor = UIColor.grayColor;
            [view addSubview:videoView];
            [videoView mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.edges.equalTo(view);
            }];
        }
        else {
            view.hidden = YES;
        }
    }
}

#pragma mark - getter
- (UIStackView *)stackView {
    if (!_stackView) {
        _stackView = [[UIStackView alloc] init];
        _stackView.axis = UILayoutConstraintAxisHorizontal;
        _stackView.alignment = UIStackViewAlignmentCenter;
        _stackView.spacing = 3.5;
    }
    return _stackView;
}

- (NSMutableArray<UIView *> *)viewArray {
    if (!_viewArray) {
        _viewArray = [NSMutableArray array];
    }
    return _viewArray;
}

@end
