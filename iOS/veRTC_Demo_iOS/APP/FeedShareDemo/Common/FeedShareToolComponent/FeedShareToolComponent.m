//
//  FeedShareToolComponent.m
//  veRTC_Demo
//
//  Created by on 2022/4/7.
//  
//

#import "FeedShareToolComponent.h"
#import <CommonCrypto/CommonCrypto.h>

@implementation FeedShareToolComponent

+ (NSString *)MD5ForLower32Bate:(NSString *)str {
    const char *input = [str UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(input, (CC_LONG)strlen(input), result);
    NSMutableString *digest = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for (NSInteger i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [digest appendFormat:@"%02x", result[i]];
    }
    return digest;
}

@end
