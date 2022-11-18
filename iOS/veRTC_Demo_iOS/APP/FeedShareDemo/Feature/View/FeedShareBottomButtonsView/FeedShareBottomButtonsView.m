//
//  FeedShareBottomButtonsView.m
//  veRTC_Demo
//
//  Created by on 2022/1/5.
//  
//

#import "FeedShareBottomButtonsView.h"

@interface FeedShareBottomButtonsView ()

@property (nonatomic, strong) UIStackView *stackView;

@property (nonatomic, strong) UIButton *audioButton;
@property (nonatomic, strong) UIButton *videoButton;
@property (nonatomic, strong) UIButton *beautyButton;
@property (nonatomic, strong) UIButton *watchButton;
@property (nonatomic, strong) UIButton *settingButton;
@property (nonatomic, strong) UIActivityIndicatorView *indicatorView;

@end

@implementation FeedShareBottomButtonsView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    self.audioButton = [[UIButton alloc] init];
    self.audioButton.tag = FeedShareButtonTypeAudio;
    [self.audioButton setImage:[UIImage imageNamed:@"feed_share_room_mic" bundleName:HomeBundleName] forState:UIControlStateNormal];
    [self.audioButton setImage:[UIImage imageNamed:@"feed_share_room_mic_s" bundleName:HomeBundleName] forState:UIControlStateSelected];
    
    self.videoButton = [[UIButton alloc] init];
    self.videoButton.tag = FeedShareButtonTypeVideo;
    [self.videoButton setImage:[UIImage imageNamed:@"feed_share_login_video" bundleName:HomeBundleName] forState:UIControlStateNormal];
    [self.videoButton setImage:[UIImage imageNamed:@"feed_share_room_video_s" bundleName:HomeBundleName] forState:UIControlStateSelected];
    
    self.beautyButton = [[UIButton alloc] init];
    self.beautyButton.tag = FeedShareButtonTypeBeauty;
    [self.beautyButton setImage:[UIImage imageNamed:@"feed_share_beauty" bundleName:HomeBundleName] forState:UIControlStateNormal];
    [self.beautyButton setImage:[UIImage imageNamed:@"feed_share_beauty" bundleName:HomeBundleName] forState:UIControlStateHighlighted];
    
    self.watchButton = [[UIButton alloc] init];
    self.watchButton.tag = FeedShareButtonTypeWatch;
    [self.watchButton setImage:[UIImage imageNamed:@"feed_share_watch" bundleName:HomeBundleName] forState:UIControlStateNormal];
    [self.watchButton setImage:[UIImage imageNamed:@"feed_share_watch" bundleName:HomeBundleName] forState:UIControlStateSelected];
    
    self.settingButton = [[UIButton alloc] init];
    self.settingButton.tag = FeedShareButtonTypeSetting;
    [self.settingButton setImage:[UIImage imageNamed:@"feed_share_setting" bundleName:HomeBundleName] forState:UIControlStateNormal];
    [self.settingButton setImage:[UIImage imageNamed:@"feed_share_setting" bundleName:HomeBundleName] forState:UIControlStateSelected];
    
    [self addSubview:self.stackView];
    [self.stackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self);
        make.top.bottom.equalTo(self);
        make.height.mas_equalTo(44);
    }];
    NSArray *buttons = @[self.audioButton, self.videoButton, self.beautyButton, self.watchButton, self.settingButton];
    for (UIButton *button in buttons) {
        button.adjustsImageWhenHighlighted = NO;
        [button addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [self updateButtonColor:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(44, 44));
        }];
        [self.stackView addArrangedSubview:button];
    }
    
    [self.watchButton addSubview:self.indicatorView];
    [self.indicatorView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.watchButton);
    }];
}

- (void)setIsLoading:(BOOL)isLoading {
    _isLoading = isLoading;
    [self updateWatchButtonLoading:isLoading];
}

- (void)updateButtonColor:(UIButton *)button {
    [button setImageEdgeInsets:UIEdgeInsetsMake(11, 11, 11, 11)];
    button.imageView.contentMode = UIViewContentModeScaleAspectFit;
    button.backgroundColor = [UIColor colorWithWhite:1 alpha:0.1];
    button.layer.masksToBounds = YES;
    button.layer.cornerRadius = 44/2;
}

- (void)setType:(FeedShareButtonViewType)type {
    
    switch (type) {
        case FeedShareButtonViewTypePreView:
            self.watchButton.hidden = YES;
            self.settingButton.hidden = YES;
            break;
        case FeedShareButtonViewTypeRoom:
            self.watchButton.hidden = NO;
            self.settingButton.hidden = YES;
            break;
        case FeedShareButtonViewTypeWatch:
            self.watchButton.hidden = YES;
            self.settingButton.hidden = NO;
            break;
        default:
            break;
    }
}

#pragma mark - action
- (void)buttonClick:(UIButton *)button {
    FeedShareButtonType type = button.tag;
    if (type == FeedShareButtonTypeAudio || type == FeedShareButtonTypeVideo) {
        button.selected = !button.selected;
    }
    if ([self.delegate respondsToSelector:@selector(feedShareBottomButtonsView:didClickButtonType:)]) {
        [self.delegate feedShareBottomButtonsView:self didClickButtonType:type];
    }
}

- (BOOL)enableAudio {
    return !self.audioButton.isSelected;
}

- (void)setEnableAudio:(BOOL)enableAudio {
    self.audioButton.selected = !enableAudio;
}

- (BOOL)enableVideo {
    return !self.videoButton.isSelected;
}

- (void)setEnableVideo:(BOOL)enableVideo {
    self.videoButton.selected = !enableVideo;
}

#pragma mark - Private Action

- (void)updateWatchButtonLoading:(BOOL)isLoading {
    if (isLoading) {
        [self.indicatorView startAnimating];
        [self.watchButton setImage:nil forState:UIControlStateNormal];
    } else {
        [self.indicatorView stopAnimating];
        [self.watchButton setImage:[UIImage imageNamed:@"feed_share_watch" bundleName:HomeBundleName] forState:UIControlStateNormal];
    }
}

#pragma mark - getter

- (UIStackView *)stackView {
    if (!_stackView) {
        _stackView = [[UIStackView alloc] init];
        _stackView.axis = UILayoutConstraintAxisHorizontal;
        _stackView.alignment = UIStackViewAlignmentCenter;
        _stackView.spacing = 60;
    }
    return _stackView;
}

- (UIActivityIndicatorView *)indicatorView {
    if (_indicatorView == nil) {
        _indicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
        _indicatorView.backgroundColor = [UIColor clearColor];
        _indicatorView.hidesWhenStopped = YES;
    }
    return _indicatorView;
}

@end
