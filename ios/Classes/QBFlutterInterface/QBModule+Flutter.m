//
//  QBModule+Flutter.m
//  crossplatform-sdk
//
//  Created by Injoit on 27.12.2019.
//  Copyright © 2019 Injoit LTD. All rights reserved.
//

#import "QBModule+Flutter.h"
#import <objc/runtime.h>

static void *_qb_flutterEvents;
static void *_qb_eventSinks;

@implementation QBModule (Flutter)

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
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
}

- (void)setupRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    NSString *className = NSStringFromClass(self.class);
    NSString *channelName = [@"Flutter" stringByAppendingString:[className stringByReplacingOccurrencesOfString:@"Module" withString:@"Channel"]];
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:channelName
                                     binaryMessenger:[registrar messenger]];
    [registrar addMethodCallDelegate:(NSObject <FlutterPlugin> *)self channel:channel];
    
    for (NSString *eventName in self.events) {
        NSString *flutterEventName = [NSString stringWithFormat:@"%@/%@", channelName, eventName];
        FlutterEventChannel *eventChannel =
        [FlutterEventChannel eventChannelWithName:flutterEventName
                                  binaryMessenger:[registrar messenger]];
        [eventChannel setStreamHandler:self];
        self.flutterEvents[flutterEventName] = sdkEvent(eventName);
    }
}

- (NSMutableDictionary<NSString *, NSString *>*)flutterEvents {
    NSMutableDictionary<NSString *, NSString *>*result = objc_getAssociatedObject(self,
                                                                                  &_qb_flutterEvents);
    if (result == nil) {
        result = @{}.mutableCopy;
        objc_setAssociatedObject(self,
                                 &_qb_flutterEvents,
                                 result,
                                 OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }
    return result;
}

- (NSMutableDictionary<NSString *, FlutterEventSink>*)eventSinks
{
    NSMutableDictionary<NSString *, FlutterEventSink>*result = objc_getAssociatedObject(self,
                                                                                        &_qb_eventSinks);
    if (result == nil) {
        result = @{}.mutableCopy;
        objc_setAssociatedObject(self,
                                 &_qb_eventSinks,
                                 result,
                                 OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }
    return result;
}

- (FlutterError * _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    NSString *flutterEventName = (NSString *)arguments;

    NSString *event = self.flutterEvents[flutterEventName];
    
    if (self.eventSinks[event]) {
        [self.eventSinks removeObjectForKey:event];
        [NSNotificationCenter.defaultCenter removeObserver:self
          name:event
        object:nil];
    }
    
    return nil;
}

- (FlutterError * _Nullable)onListenWithArguments:(id _Nullable)arguments
                                        eventSink:(nonnull FlutterEventSink)events {
    NSString *flutterEventName = (NSString *)arguments;

    NSString *event = self.flutterEvents[flutterEventName];
    
    if ((!events) && self.eventSinks[event]) {
        [self.eventSinks removeObjectForKey:event];
        [NSNotificationCenter.defaultCenter removeObserver:self
                                                      name:event
                                                    object:nil];
        return nil;
    }
    
    self.eventSinks[event] = [events copy];
    
    [NSNotificationCenter.defaultCenter addObserver:self
                                           selector:@selector(didReceiveQBEventNotification:)
                                               name:event
                                             object:nil];
    return nil;
}

- (void)didReceiveQBEventNotification:(NSNotification *) notification {
    FlutterEventSink eventSink = self.eventSinks[notification.name];
    if (!eventSink) {
        return;
    }
    
    if ([notification.object isKindOfClass:NSDictionary.class]) {
        NSDictionary *object = notification.object;
        NSMutableDictionary *data = object.mutableCopy;
        NSString *type = data[QBBridgeEventKey.type];
        // Remove QB substring from event name
        if (type.length) {
            NSString *subsctring = @"";
            if ([type containsString:@"QB/"]) {
                subsctring = @"QB/";
            }
            if ([type containsString:@"@QB/"]) {
                subsctring = @"@QB/";
            }
            if (subsctring.length) {
                NSString *clearType = [type stringByReplacingOccurrencesOfString:subsctring withString:@""];
                data[QBBridgeEventKey.type] = clearType;
                eventSink(data.copy);
                return;
            }
        }
    }
    
    eventSink(notification.object);
}

@end
