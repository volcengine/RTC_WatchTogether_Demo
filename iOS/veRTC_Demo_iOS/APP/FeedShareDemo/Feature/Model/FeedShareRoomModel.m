//
//  FeedShareRoomModel.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/5.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedShareRoomModel.h"

@implementation FeedShareRoomModel

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{@"appID" : @"app_id",
             @"roomID" : @"room_id",
             @"hostUid" : @"host_user_id",
             @"hostName" : @"host_user_name",
             @"roomStatus" : @"room_scene",
             @"rtcToken" : @"rtc_token",
             @"videoList" : @"content_list",
    };
}

+ (NSDictionary *)modelContainerPropertyGenericClass {

    return @{
        @"videoList" : [FeedShareVideoModel class],  
    };

}

@end
