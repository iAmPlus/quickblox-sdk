//
//  QBSettingsModule+Flutter.m
//  crossplatform-sdk
//
//  Created by Injoit on 27.12.2019.
//  Copyright © 2019 Injoit LTD. All rights reserved.
//

#import "QBSettingsModule+Flutter.h"

@implementation QBSettingsModule (Flutter)

+ (void)registerWithRegistrar:(nonnull NSObject<FlutterPluginRegistrar> *)registrar {
    QBSettingsModule* instance = [[QBSettingsModule alloc] init];
    [instance setupRegistrar:registrar];
}

@end
