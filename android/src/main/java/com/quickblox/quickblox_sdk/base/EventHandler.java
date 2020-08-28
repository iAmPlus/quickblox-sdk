package com.quickblox.quickblox_sdk.base;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class EventHandler implements EventChannel.StreamHandler {

    private static Set<EventHandler> eventHandlers = new HashSet<>();

    private EventChannel.EventSink eventSink;
    private String eventName;

    public static void init(List<String> eventNames, BinaryMessenger binaryMessenger) {
        for (String event : eventNames) {
            init(event, binaryMessenger);
        }
    }

    public static void init(String eventName, BinaryMessenger binaryMessenger) {
        EventHandler eventHandler = new EventHandler(eventName);
        eventHandler.init(binaryMessenger);
        eventHandlers.add(eventHandler);
    }

    public static <T> void sendEvent(String eventName, T data) {
        if (!eventHandlers.contains(new EventHandler(eventName))) {
            return;
        }

        for (EventHandler eventHandler : eventHandlers) {
            if (eventHandler.eventName.equals(eventName) && eventHandler.eventSink != null) {
                eventHandler.eventSink.success(data);
                break;
            }
        }
    }

    private EventHandler(String eventName) {
        this.eventName = eventName;
    }

    private void init(BinaryMessenger binaryMessenger) {
        new EventChannel(binaryMessenger, eventName).setStreamHandler(this);
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink eventSink) {
        if (arguments != null) {
            this.eventName = arguments.toString();
        }
        this.eventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {
        this.eventSink = null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean equals;
        if (obj instanceof EventHandler) {
            equals = this.eventName.equals(((EventHandler) obj).eventName);
        } else {
            equals = super.equals(obj);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + eventName.hashCode();
        return hash;
    }
}