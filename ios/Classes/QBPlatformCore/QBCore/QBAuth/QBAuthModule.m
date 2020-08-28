//
//  QBAuthModule.m
//  crossplatform-sdk
//
//  Created by Injoit on 23.12.2019.
//  Copyright © 2019 Injoit LTD. All rights reserved.
//

#import "QBAuthModule.h"
#import "QBAuthConstants.h"
#import "QBSession+QBSerializer.h"
#import "QBUUser+QBSerializer.h"
#import "NSDate+Helper.h"

@implementation QBAuthModule

- (void)login:(NSDictionary *)info
     resolver:(QBResolveBlock)resolve
     rejecter:(QBRejectBlock)reject {
    
     NSObject *loginObject = info[AuthKey.login];
     if ([NSError reject:reject
            checkerClass:NSString.class
                  object:loginObject
               objectKey:AuthKey.login]) {
         return;
     }
     NSString *login = (NSString *)loginObject;
     
     NSObject *passwordObject = info[AuthKey.password];
     if ([NSError reject:reject
            checkerClass:NSString.class
                  object:passwordObject
               objectKey:AuthKey.password]) {
         return;
     }
     NSString *password = (NSString *)passwordObject;
     [QBRequest logInWithUserLogin:login
                          password:password
                      successBlock:^(QBResponse *response,
                                     QBUUser *user) {
                          NSError *error = nil;
                          NSDictionary *sessionData = [[QBSession currentSession] toQBResultData:&error];
                          NSDictionary *userData = [user toQBResultData:&error];
                          if ([error reject:reject]) {
                              return;
                          }
                          if (resolve) {
                              resolve(@{ AuthKey.user: userData,
                                         AuthKey.session: sessionData });
                          }
                      } errorBlock:^(QBResponse *response) {
                          [response reject:reject];
                      }];
}

- (void)logout:(QBResolveBlock)resolve
      rejecter:(QBRejectBlock)reject {
    [QBRequest logOutWithSuccessBlock:^(QBResponse *response) {
        if (resolve) {
            resolve(nil);
        }
    } errorBlock:^(QBResponse *response) {
        [response reject:reject];
    }];
}

- (void)createSession:(NSDictionary *)info
             resolver:(QBResolveBlock)resolve
             rejecter:(QBRejectBlock)reject {
    NSObject *tokenObject = info[QBSessionKey.token];
    if ([NSError reject:reject
           checkerClass:NSString.class
                 object:tokenObject
              objectKey:QBSessionKey.token]) {
        return;
    }
    NSString *token = (NSString *)tokenObject;
    
    NSObject *expirationDateObject = info[QBSessionKey.expirationDate];
    if ([NSError reject:reject
           checkerClass:NSString.class
                 object:expirationDateObject
              objectKey:QBSessionKey.expirationDate]) {
        return;
    }
    NSString *expirationDate = (NSString *)expirationDateObject;
    QBASession *session = [[QBASession alloc] init];
    session.token = token;
    NSDate *date = [NSDate dateFromQBTokenHeader:expirationDate];
    QBSession *currentSession = [QBSession currentSession];
    [currentSession startSessionWithDetails:session
                             expirationDate:date];
    [currentSession toQBResultDataWithResolver:resolve rejecter:reject];
}

- (void)getSession:(QBResolveBlock)resolve
          rejecter:(QBRejectBlock)reject {
    NSError *error = nil;
    NSDictionary *data = [[QBSession currentSession] toQBResultData:&error];
    if ([error reject:reject]) {
        return;
    }
    if (resolve) {
        resolve(data);
    }
}

@end
