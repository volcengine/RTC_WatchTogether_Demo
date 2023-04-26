// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
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
