package com.quickblox.quickblox_sdk.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class EventsUtil {
    private static final String PAYLOAD = "payload";
    private static final String TYPE = "type";

    private EventsUtil() {
        //empty
    }

    public static <T> Map buildResult(String eventName, T payload) {
        Map<String, Object> map = new HashMap<>();

        if (!TextUtils.isEmpty(eventName)) {
            map.put(TYPE, eventName);
        }

        if (payload != null) {
            map.put(PAYLOAD, payload);
        }
        return map;
    }
}