//
//  FeedShareDemo.m
//  FeedShareDemo
//
//  Created by on 2022/5/5.
//

#import "FeedShareDemo.h"
#import "JoinRTSParams.h"
#import "FeedShareRTCManager.h"
#import <Core/NetworkReachabilityManager.h>
#import "FeedShareCreateRoomViewController.h"

@implementation FeedShareDemo

- (void)pushDemoViewControllerBlock:(void (^)(BOOL result))block {
    [super pushDemoViewControllerBlock:block];
    
    // 获取登录 RTS 参数
    // Get login RTS parameters
    JoinRTSInputModel *inputModel = [[JoinRTSInputModel alloc] init];
    inputModel.scenesName = @"tw";
    inputModel.loginToken = [LocalUserComponent userModel].loginToken;
    inputModel.contentPartner = ContentPartner;
    inputModel.contentCategory = ContentCategory;
    __weak __typeof(self) wself = self;
    [JoinRTSParams getJoinRTSParams:inputModel
                             block:^(JoinRTSParamsModel * _Nonnull model) {
        [wself joinRTS:model block:block];
    }];
}

- (void)joinRTS:(JoinRTSParamsModel * _Nonnull)model
          block:(void (^)(BOOL result))block{
    if (!model) {
        [[ToastComponent shareToastComponent] showWithMessage:@"连接失败"];
        if (block) {
            block(NO);
        }
        return;
    }
    // Connect RTS
    [[FeedShareRTCManager shareRtc] connect:model.appId
                                   RTSToken:model.RTSToken
                                  serverUrl:model.serverUrl
                                  serverSig:model.serverSignature
                                        bid:model.bid
                                      block:^(BOOL result) {
        if (result) {
            FeedShareCreateRoomViewController *next = [[FeedShareCreateRoomViewController alloc] init];
            UIViewController *topVC = [DeviceInforTool topViewController];
            [topVC.navigationController pushViewController:next animated:YES];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:@"连接失败"];
        }
        if (block) {
            block(result);
        }
    }];
}

@end
