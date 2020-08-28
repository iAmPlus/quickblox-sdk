//
//  QBWebRTCFlutterVideoView.m
//  crossplatform-sdk
//
//  Created by Illia Chemolosov on 10.01.2020.
//  Copyright © 2020 Injoit LTD. All rights reserved.
//

#import "QBWebRTCFlutterVideoView.h"
#import "QBWebRTCVideoView.h"

struct QBWebRTCViewKeysStruct {
    __unsafe_unretained NSString * const mirror;
    __unsafe_unretained NSString * const scalingType;
    __unsafe_unretained NSString * const userId;
    __unsafe_unretained NSString * const sessionId;
};
extern const struct QBWebRTCViewKeysStruct QBWebRTCViewKey;

const struct QBWebRTCViewKeysStruct QBWebRTCViewKey = {
    .mirror = @"mirror",
    .scalingType = @"scalingType",
    .userId = @"userId",
    .sessionId = @"sessionId",
};

typedef NS_ENUM(NSInteger, QBRTCViewScaleType) {
    QBRTCViewScaleTypeFill,
    QBRTCViewScaleTypeFit,
    QBRTCViewScaleTypeAuto
};

@interface QBWebRTCFlutterVideoView ()

@property (nonatomic, strong) QBWebRTCVideoView *view;
@property (nonatomic, assign) int64_t viewId;

@end

@implementation QBWebRTCFlutterVideoView

+ (instancetype)viewWithMessenger:(NSObject<FlutterBinaryMessenger> *)messenger
                            frame:(CGRect)frame
                   viewIdentifier:(int64_t)viewId {
    return [[QBWebRTCFlutterVideoView alloc] initWithBinaryMessenger:messenger
                                                               frame:frame
                                                      viewIdentifier:viewId];
}

- (instancetype)initWithBinaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger
                                  frame:(CGRect)frame
                         viewIdentifier:(int64_t)viewId  {
    self = [super init];
    if (self) {
        _view = [[QBWebRTCVideoView alloc] initWithFrame:frame];
        _viewId = viewId;
        
        NSString *className = NSStringFromClass(self.class);
        NSString *channelName = [className stringByAppendingString:[NSString stringWithFormat:@"Channel/%@", @(viewId)]];
        FlutterMethodChannel* channel = [FlutterMethodChannel
                                         methodChannelWithName:channelName
                                         binaryMessenger:messenger];
        [channel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call,
                                        FlutterResult  _Nonnull result) {
            NSString *getSelectorString = [call.method stringByAppendingFormat:@":rejecter:"];
            NSString *setSelectorString = [call.method stringByAppendingFormat:@":resolver:rejecter:"];
            SEL getSelector = NSSelectorFromString(getSelectorString);
            SEL setSelector = NSSelectorFromString(setSelectorString);
            QBResolveBlock resolve = ^(id _Nullable qbResult) {
                result(qbResult);
            };
            
            QBRejectBlock reject = ^(NSString * _Nonnull code,
                                     NSString * _Nullable message,
                                     NSError * _Nullable error) {
                result([FlutterError errorWithCode:[[code stringByAppendingString:@"\n"] stringByAppendingString:message] message:nil details:error.localizedDescription]);
            };
            
            NSMutableDictionary *args = @{}.mutableCopy;
            id arguments = call.arguments;
            if ([arguments isKindOfClass:NSDictionary.class]) {
                NSDictionary *objectData = (NSDictionary *)arguments;
                for (NSString *key in objectData.allKeys) {
                    id value = objectData[key];
                    if (![value isKindOfClass:NSNull.class]) {
                        args[key] = value;
                    }
                }
            }
            
            NSDictionary *objectValue = args.copy;
            
            if ([self respondsToSelector:setSelector]) {
                NSMethodSignature *signature  = [self methodSignatureForSelector:setSelector];
                NSInvocation      *invocation = [NSInvocation invocationWithMethodSignature:signature];
                [invocation setTarget:self];
                [invocation setSelector:setSelector];
                [invocation setArgument:&objectValue atIndex:2];
                [invocation setArgument:&resolve atIndex:3];
                [invocation setArgument:&reject atIndex:4];
                
                [invocation invoke];
            } else if ([self respondsToSelector:getSelector]) {
                NSMethodSignature *signature  = [self methodSignatureForSelector:getSelector];
                NSInvocation      *invocation = [NSInvocation invocationWithMethodSignature:signature];
                [invocation setTarget:self];
                [invocation setSelector:getSelector];
                [invocation setArgument:&resolve atIndex:2];
                [invocation setArgument:&reject atIndex:3];
                
                [invocation invoke];
            } else {
                result(FlutterMethodNotImplemented);
            }
        }];
    }
    return self;
}

- (void)mirror:(NSDictionary *)info
      resolver:(QBResolveBlock)resolve
      rejecter:(QBRejectBlock)reject {
    NSObject *mirrorObject = info[QBWebRTCViewKey.mirror];
    //    if ([NSError reject:reject
    //           checkerClass:NSNumber.class
    //                 object:mirrorObject
    //              objectKey:QBWebRTCViewKey.mirror]) {
    //        return;
    //    }
    NSNumber *mirrorNumber = (NSNumber *)mirrorObject;
    if (!mirrorNumber) {
        self.view.mirror = NO;
    } else {
        self.view.mirror = mirrorNumber.boolValue;
    }
    if (resolve) {
        resolve(nil);
    }
}

- (void)scaleType:(NSDictionary *)info
         resolver:(QBResolveBlock)resolve
         rejecter:(QBRejectBlock)reject {
    NSNumber *scaleTypeNumber = info[QBWebRTCViewKey.scalingType];
    QBRTCViewScaleType scaleType = scaleTypeNumber.integerValue;
    switch (scaleType) {
        case QBRTCViewScaleTypeFill: {
            self.view.videoGravity = AVLayerVideoGravityResizeAspectFill;
            break;
        }
        case QBRTCViewScaleTypeFit: {
            self.view.videoGravity = AVLayerVideoGravityResizeAspect;
            break;
        }
        case QBRTCViewScaleTypeAuto: {
            self.view.videoGravity = AVLayerVideoGravityResize;
            break;
        }
    }
    
    if (resolve) {
        resolve(nil);
    }
}

- (void)play:(NSDictionary *)info
    resolver:(QBResolveBlock)resolve
    rejecter:(QBRejectBlock)reject {
    NSObject *userIdObject = info[QBWebRTCViewKey.userId];
    if ([NSError reject:reject
           checkerClass:NSNumber.class
                 object:userIdObject
              objectKey:QBWebRTCViewKey.mirror]) {
        return;
    }
    NSNumber *userId = (NSNumber *)userIdObject;
    
    NSObject *sessionIdObject = info[QBWebRTCViewKey.sessionId];
    if ([NSError reject:reject
           checkerClass:NSString.class
                 object:sessionIdObject
              objectKey:QBWebRTCViewKey.sessionId]) {
        return;
    }
    NSString *sessionId = (NSString *)sessionIdObject;
    
    if (![self.delegate respondsToSelector:@selector(cache)]) {
        NSLog(@"Cannot find QBWebRTCSessionControllerCache");
        return;
    }
    
    QBWebRTCSessionController *sessionController = self.delegate.cache[sessionId];
    
    if (!sessionController.session || !sessionController.currentUserId) {
        NSLog(@"Cannot find QBRTCSession with id #%@", sessionId);
        return;
    }
    
    if ([userId isEqual:sessionController.currentUserId] && !self.view.videoLayer) {
        // Setup local stream
        QBRTCCameraCapture *videoCapture = sessionController.videoCapture;
        videoCapture.previewLayer.videoGravity = self.view.videoGravity;
        self.view.videoLayer = videoCapture.previewLayer;
        [sessionController.videoCapture startSession:nil];
    } else {
        // Setup remote stream
        QBRTCVideoTrack *videoTrack = [sessionController.session remoteVideoTrackWithUserID:userId];
        if (!videoTrack) { return; }
        self.view.videoTrack = videoTrack;
    }
    self.view.transform = CGAffineTransformMakeScale(self.view.mirror ? -1.0 : 1.0, 1.0);
}

- (void)release:(NSDictionary *)info
       resolver:(QBResolveBlock)resolve
       rejecter:(QBRejectBlock)reject {
    //This method need for the Android side only
    if (resolve) {
        resolve(nil);
    }
}

@end
