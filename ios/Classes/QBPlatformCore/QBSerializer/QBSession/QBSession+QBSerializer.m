//
//  QBSession+QBSerializer.m
//  crossplatform-sdk
//
//  Created by Injoit on 25.12.2019.
//  Copyright © 2019 Injoit LTD. All rights reserved.
//

#import "QBSession+QBSerializer.h"
#import "NSDate+Helper.h"

const struct QBSessionKeysStruct QBSessionKey = {
    .token = @"token",
    .expirationDate = @"expirationDate",
};

@implementation QBSession (QBSerializer)

- (id)toQBResultData:(NSError *__autoreleasing *)error {
    NSMutableDictionary *info = @{}.mutableCopy;
    
    if (self.sessionDetails.token.length) {
        info[QBSessionKey.token] = self.sessionDetails.token;
    }
    
    NSString *expirationDate = [NSDate stringFromISO8601Date:self.sessionExpirationDate];
    if (expirationDate.length) {
        info[QBSessionKey.expirationDate] = expirationDate;
    }
    
    return [info.copy toQBResultWithType:QBResultTypeDefault error:error];
}

@end
