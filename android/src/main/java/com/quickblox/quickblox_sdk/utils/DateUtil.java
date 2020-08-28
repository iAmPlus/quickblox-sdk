package com.quickblox.quickblox_sdk.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class DateUtil {

    private DateUtil() {
        //empty
    }

    public static String convertDateToISO(Date date) {
        //Convert Date to ISO 8601
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String convertedDate = dateFormat.format(date);
        return convertedDate;
    }
}