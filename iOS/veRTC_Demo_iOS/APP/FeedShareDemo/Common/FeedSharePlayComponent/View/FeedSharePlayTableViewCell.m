//
//  FeedSharePlayTableViewCell.m
//  veRTC_Demo
//
//  Created by on 2022/1/6.
//  
//

#import "FeedSharePlayTableViewCell.h"
#import "UIImageView+WebCache.h"
#import "FeedShareVideoModel.h"

@interface FeedSharePlayTableViewCell ()

@property (nonatomic, strong) FeedSharePlayerComponent *player;
@property (nonatomic, strong) UIView *videoView;
@property (nonatomic, strong) UIImageView *coverImageView;
@property (nonatomic, strong) UIImageView *pauseImageView;

@end

@implementation FeedSharePlayTableViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        [self setupView];
        
        self.contentView.backgroundColor = UIColor.blackColor;
        
        [self.contentView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewTouch)]];
    }
    return self;
}

- (void)setupView {
    
    [self.contentView addSubview:self.coverImageView];
    [self.coverImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.contentView);
    }];
    [self.contentView addSubview:self.pauseImageView];
    [self.pauseImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self.contentView);
    }];
    [self.contentView addSubview:self.videoView];
}

- (void)configPlayer {
    if (!_player) {
        _player = [[FeedSharePlayerComponent alloc] initWithPlayerView:self.videoView];
        [self.contentView bringSubviewToFront:self.pauseImageView];
        _player.videoModel = self.videoModel;
    }
}

- (void)prepareForReuse {
    [super prepareForReuse];
}

#pragma mark - action
- (void)viewTouch {
    if (self.player.isPlaying) {
        [self.player pause];
        self.pauseImageView.hidden = NO;
        if (self.videoStateChangedBlock) {
            self.videoStateChangedBlock(YES);
        }
    }
    else {
        [self.player play];
        self.pauseImageView.hidden = YES;
        if (self.videoStateChangedBlock) {
            self.videoStateChangedBlock(NO);
        }
    }
    
}

#pragma mark - methdos
- (void)setVideoModel:(FeedShareVideoModel *)videoModel{
    _videoModel = videoModel;
    
    if (videoModel.coverURL) {
        [self.coverImageView sd_setImageWithURL:[NSURL URLWithString:videoModel.coverURL] placeholderImage:nil];
    }
    [self configPlayer];
    self.player.videoModel = self.videoModel;
}

- (void)play {
    
    if ([self.player isPlaying]) {
        return;
    }
    
    if (![self.player isPaused]) {
        [self configPlayer];
        self.player.videoModel = self.videoModel;
    }
    [self.player play];
    self.pauseImageView.hidden = YES;
}

- (void)pause {
    [self.player pause];
    self.pauseImageView.hidden = NO;
}

- (void)stop {
    [self.player stop];
}

#pragma mark - getter

- (UIView *)videoView {
    if (!_videoView) {
        _videoView = [[UIView alloc] initWithFrame:UIScreen.mainScreen.bounds];
    }
    return _videoView;
}

- (UIImageView *)coverImageView {
    if (!_coverImageView) {
        _coverImageView = [[UIImageView alloc] init];
        _coverImageView.contentMode = UIViewContentModeScaleAspectFill;
    }
    return _coverImageView;
}

- (UIImageView *)pauseImageView {
    if (!_pauseImageView) {
        _pauseImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"feed_share_pause" bundleName:HomeBundleName]];
        _pauseImageView.hidden = YES;
    }
    return _pauseImageView;
}

@end
