//
//  FeedShareDemo.m
//  FeedShareDemo
//
//  Created by bytedance on 2022/5/5.
//

#import "FeedShareDemo.h"
#import "FeedShareRTCManager.h"
#import <Core/NetworkReachabilityManager.h>
#import "FeedShareCreateRoomViewController.h"

@implementation FeedShareDemo

- (void)pushDemoViewControllerBlock:(void (^)(BOOL result))block {
    [FeedShareRTCManager shareRtc].networkDelegate = [NetworkReachabilityManager sharedManager];
    [[FeedShareRTCManager shareRtc] connect:@"tw"
                                 loginToken:[LocalUserComponents userModel].loginToken
                             contentPartner:ContentPartner
                            contentCategory:ContentCategory
                                      block:^(BOOL result) {
        if (result) {
            FeedShareCreateRoomViewController *next = [[FeedShareCreateRoomViewController alloc] init];
            UIViewController *topVC = [DeviceInforTool topViewController];
            [topVC.navigationController pushViewController:next animated:YES];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:@"连接失败"];
        }
        if (block) {
            block(result);
        }
    }];
}

@end
