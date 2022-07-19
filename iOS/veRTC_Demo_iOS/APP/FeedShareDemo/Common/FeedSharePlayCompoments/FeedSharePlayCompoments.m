//
//  FeedSharePlayCompoments.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/6.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedSharePlayCompoments.h"
#import "FeedShareMessageComponent.h"
#import "FeedShareRTMManager.h"
#import "FeedShareToolComponent.h"

#import "FeedSharePlayTableViewCell.h"
#import "TTVideoEngineHeader.h"
#import "UITableView+FeedShare.h"
#import "GCDTimer.h"

@interface FeedSharePlayCompoments ()<UITableViewDelegate, UITableViewDataSource, FeedShareMessageComponentDelegate>

@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) NSArray<FeedShareVideoModel*> *dataArray;

@property (nonatomic, assign) CGPoint targetOffset;

@property (nonatomic, assign) BOOL isRequesting;

@property (nonatomic, strong) FeedShareRoomModel *roomModel;
@property (nonatomic, assign) BOOL isHost;

@property (nonatomic, strong) NSIndexPath *currentPlayIndexPath;
@property (nonatomic, strong) FeedSharePlayTableViewCell *currentPlayCell;
@property (nonatomic, strong) NSIndexPath *willPlayIndexPath;

@property (nonatomic, assign) BOOL shouldPlayInAdvance;
@property (nonatomic, assign) BOOL isDragging;
@property (nonatomic, assign) CGFloat lastContentOffsetY;
@property (nonatomic, assign) NSTimeInterval lastCheckPlayTimeInAdvance;

@property (nonatomic, strong) GCDTimer *timer;
@property (nonatomic, strong) FeedShareMessageComponent *messageComponent;

@end

@implementation FeedSharePlayCompoments

- (instancetype)initWithRoomModel:(FeedShareRoomModel *)roomModel {
    if (self = [super init]) {
        self.roomModel = roomModel;
        self.isHost = [roomModel.hostUid isEqualToString:[LocalUserComponents userModel].uid];
        
        self.shouldPlayInAdvance = YES;
        
        [self tableView];
    
        self.messageComponent = [[FeedShareMessageComponent alloc] initWithDelegate:self];
        
        self.tableView.userInteractionEnabled = self.isHost;
    }
    return self;
}

- (void)preloadVideoWithVideoList:(NSArray<FeedShareVideoModel *> *)videoList refresh:(BOOL)refresh {
    NSMutableArray *sources = [NSMutableArray array];
    [videoList enumerateObjectsUsingBlock:^(FeedShareVideoModel * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [sources addObject:[FeedShareVideoModel videoEngineUrlSource:obj]];
    }];
    if (refresh) {
        [TTVideoEngine setStrategyVideoSources:sources];
    } else {
        [TTVideoEngine addStrategyVideoSources:sources];
    }
}

#pragma mark - FeedShareMessageComponentDelegate
- (void)feedShareMessageComponent:(FeedShareMessageComponent *)messageComponent didReceivedVideoMessageModel:(FeedShareVideoMessageModel *)videoMessageModel {
    
    [self scrollToTargetVideo:videoMessageModel animation:NO];
}

#pragma mark - UITableView delegate

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    FeedSharePlayTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([FeedSharePlayTableViewCell class]) forIndexPath:indexPath];
    cell.videoModel = self.dataArray[indexPath.row];

    __weak typeof(self) weakSelf = self;
    cell.videoStateChangedBlock = ^(BOOL isPause) {
        [weakSelf sendVideoPlayStatusMessage];
    };
    return cell;
}

- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath {
    self.willPlayIndexPath = indexPath;
    FeedSharePlayTableViewCell *currentCell = (FeedSharePlayTableViewCell *)cell;
    currentCell.videoModel = [self.dataArray objectAtIndex:indexPath.row];
    
    //如果是主播并且在将要展示的数据小于3个的时候预加载下一页视频数据
    //If it is an anchor and preloads the next page of video data when the data to be displayed is less than 3
    if (self.isHost && self.dataArray.count - indexPath.row <= 3 && !self.isRequesting) {
        [self loadMoreData];
    }
}

- (void)tableView:(UITableView *)tableView didEndDisplayingCell:(nonnull UITableViewCell *)cell forRowAtIndexPath:(nonnull NSIndexPath *)indexPath {
    FeedSharePlayTableViewCell *currentCell = (FeedSharePlayTableViewCell *)cell;
    [currentCell stop];
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    if (self.isDragging) {
        return;
    }
    
    CGFloat oldContentOffsetY = self.lastContentOffsetY;
    self.lastContentOffsetY = scrollView.contentOffset.y;
    
    NSTimeInterval now = [[NSDate date] timeIntervalSince1970];
    if (now - self.lastCheckPlayTimeInAdvance < 0.05) {
        return;
    }
    self.lastCheckPlayTimeInAdvance = now;
    
    if (self.lastContentOffsetY > oldContentOffsetY) {
        // scroll up
        if (self.currentPlayIndexPath.row < self.willPlayIndexPath.row) {
            NSIndexPath *currentIndexPath = [self.tableView currentIndexPathForFullScreenCell];
            if (currentIndexPath.row == self.willPlayIndexPath.row) {
                [self playNextVideoInAdvance];
            }
        }
    } else {
        // scroll down
        if (self.currentPlayIndexPath.row > self.willPlayIndexPath.row) {
            NSIndexPath *currentIndexPath = [self.tableView currentIndexPathForFullScreenCell];
            if (currentIndexPath.row == self.willPlayIndexPath.row) {
                [self playNextVideoInAdvance];
            }
        }
    }
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    self.isDragging = YES;
    NSLog(@"playWithIndexPathMessage-BeginDragging");
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    [self onScrollDidEnd];
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    self.isDragging = NO;
    if (!decelerate) {
        [self onScrollDidEnd];
    }
}


#pragma mark - methods

- (void)updateVideoList:(NSArray<FeedShareVideoModel*> *)videoList {
    if (videoList.count == 0) {
        return;
    }
    NSMutableArray *tempArray = self.dataArray? self.dataArray.mutableCopy:[NSMutableArray array];
    [tempArray addObjectsFromArray:videoList];
    
    NSInteger index = self.dataArray.count;
    self.dataArray = tempArray.copy;
    
    NSMutableArray *insertIndexPaths = [NSMutableArray array];
    for (NSInteger i = index; i < self.dataArray.count; i++) {
        [insertIndexPaths addObject:[NSIndexPath indexPathForRow:i inSection:0]];
    }
    [self.tableView beginUpdates];
    [self.tableView insertRowsAtIndexPaths:insertIndexPaths withRowAnimation:UITableViewRowAnimationNone];
    [self.tableView endUpdates];
    [UIView setAnimationsEnabled:YES];
    
    NSLog(@"updateVideoList");

    [self preloadVideoWithVideoList:videoList refresh:index == 0];
    
    if (index == 0) {
        [self.tableView reloadData];
        [self play];
        [self startTimer];
    }
}

- (void)play {
    NSIndexPath *indexPath = [self.tableView currentIndexPathForFullScreenCell];
    if (self.currentPlayIndexPath && self.currentPlayIndexPath.row == indexPath.row) {
        return;
    }
    [self playWithIndexPath:indexPath];
}

- (void)playWithIndexPath:(NSIndexPath *)indexPath {
    if (indexPath && indexPath.row < self.dataArray.count) {
        FeedSharePlayTableViewCell *cell = [self.tableView cellForRowAtIndexPath:indexPath];
        if (self.currentPlayCell) {
            [self.currentPlayCell stop];
            self.currentPlayCell = nil;
        }
        
        [cell play];
        self.currentPlayCell = cell;
        self.currentPlayIndexPath = indexPath;
    }
}

- (void)playNextVideoInAdvance {
    if (!self.shouldPlayInAdvance) {
        return;
    }
    if (self.currentPlayIndexPath.row != [self.tableView currentIndexPathForFullScreenCell].row) {
        [self.currentPlayCell stop];
    }
    
    [self play];
    if (self.isHost) {
        [self sendVideoPlayStatusMessage];
    }
}

- (void)onScrollDidEnd {
    [self play];
    if (self.isHost) {
        NSLog(@"playWithIndexPath-hostDraggEnd");
        [self sendVideoPlayStatusMessage];
    }
}

- (void)startTimer {
    if (_timer || !self.isHost) {
        return;
    }
    _timer = [[GCDTimer alloc] init];
    __weak typeof(self) weakSelf = self;
    [_timer startTimerWithSpace:1.0 block:^(BOOL result) {
        [weakSelf sendVideoPlayStatusMessage];
    }];
}

- (void)sendVideoPlayStatusMessage {
    FeedShareVideoMessageModel *videoMessageModel = [self getVideoMessageModel];
    if (videoMessageModel) {
        [self.messageComponent sendMessage:videoMessageModel];
    }
}

- (FeedShareVideoMessageModel *)getVideoMessageModel {
    FeedSharePlayerComponent *player = self.currentPlayCell.player;
    if (!player || !self.isHost) {
        return nil;
    }
    FeedShareVideoMessageModel *videoMessageModel = [[FeedShareVideoMessageModel alloc] init];
    videoMessageModel.videoID = self.currentPlayCell.videoModel.videoId;
    videoMessageModel.totalDuration = [player totalDuration];
    videoMessageModel.currentDuration = [player currentDuration];
    videoMessageModel.videoStatus = [player isPaused]? FeedShareVideoStatusPause:FeedShareVideoStatusPlay;
    return videoMessageModel;
}

- (void)scrollToTargetVideo:(FeedShareVideoMessageModel *)videoUpdate animation:(BOOL)animation {
    if (![self.currentPlayCell.videoModel.videoId isEqualToString:videoUpdate.videoID]) {
        for (int i = 0; i < self.dataArray.count; i++) {
            if ([self.dataArray[i].videoId isEqualToString:videoUpdate.videoID]) {
                [self.currentPlayCell stop];
                NSIndexPath *indexPath = [NSIndexPath indexPathForRow:i inSection:0];
                [self.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:animation];
                [self playWithIndexPath:indexPath];
                [self updateVideoState:videoUpdate];
                break;
            }
        }
    }
    else {
        [self updateVideoState:videoUpdate];
    }
}

- (void)updateVideoState:(FeedShareVideoMessageModel *)videoUpdate {
    FeedSharePlayerComponent *player = self.currentPlayCell.player;
    
    // 比主播快, 考虑到主播重播时嘉宾还在结尾
    if (videoUpdate.currentDuration - player.currentDuration > 2 || (videoUpdate.currentDuration > 4 && player.currentDuration - videoUpdate.currentDuration > 2)) {
        [player setCurrentPlaybackTime:videoUpdate.currentDuration complete:^{
            
        }];
    }
    
    if (videoUpdate.videoStatus == FeedShareVideoStatusPause && ![self.currentPlayCell.player isPaused]) {
        [self.currentPlayCell pause];
    } else if (videoUpdate.videoStatus == FeedShareVideoStatusPlay && ![self.currentPlayCell.player isPlaying]) {
        [self.currentPlayCell play];
    }
}

- (void)loadMoreData {
    self.isRequesting = YES;
    __weak typeof(self) weakSelf = self;
    
    [FeedShareRTMManager requestVideoListWithRoomID:self.roomModel.roomID block:^(NSArray<FeedShareVideoModel *> * _Nonnull videoList, RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponents shareToastComponents] showWithMessage:model.message];
        } else {
            [weakSelf updateVideoList:videoList];
        }
        weakSelf.isRequesting = NO;
    }];
}

- (void)dealloc {
    [self.timer stopTimer];
    self.timer = nil;
}

- (void)destroy {
    // 保证先释放播放器再释放cell
    // Make sure to release the player first and then release the cell
    for (FeedSharePlayTableViewCell *cell in self.tableView.visibleCells) {
        [cell stop];
    }
}

#pragma mark - getter

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
        _tableView.backgroundColor = UIColor.blackColor;
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.rowHeight = UIScreen.mainScreen.bounds.size.height;
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.estimatedRowHeight = 0;
        _tableView.estimatedSectionHeaderHeight = 0;
        _tableView.estimatedSectionFooterHeight = 0;
        
        _tableView.pagingEnabled = YES;
        _tableView.scrollsToTop = NO;
        _tableView.showsVerticalScrollIndicator = NO;
        if (@available(iOS 11.0, *)) {
            _tableView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
        } else {
            // Fallback on earlier versions
        }
        if (@available(iOS 15.0, *)) {
            _tableView.sectionHeaderTopPadding = 0;
        } else {
            
        }

        
        [_tableView registerClass:[FeedSharePlayTableViewCell class] forCellReuseIdentifier:NSStringFromClass([FeedSharePlayTableViewCell class])];
    }
    return _tableView;
}

@end
