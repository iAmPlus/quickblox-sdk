//
//  QBSession+QBSerializer.h
//  crossplatform-sdk
//
//  Created by Injoit on 25.12.2019.
//  Copyright © 2019 Injoit LTD. All rights reserved.
//

#import <Quickblox/Quickblox.h>
#import "QBSerializerProtocol.h"

struct QBSessionKeysStruct {
    __unsafe_unretained NSString * _Nonnull const token;
    __unsafe_unretained NSString * _Nonnull const expirationDate;
};
extern const struct QBSessionKeysStruct QBSessionKey;

NS_ASSUME_NONNULL_BEGIN

@interface QBSession (QBSerializer) <QBSerializerProtocol>

@end

NS_ASSUME_NONNULL_END
