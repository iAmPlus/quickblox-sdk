
package com.quickblox.quickblox_sdk.auth;

import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.quickblox_sdk.base.BaseModule;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class AuthModule implements BaseModule {

    private static final String CHANNEL_NAME = "FlutterQBAuthChannel";

    private static final String LOGIN_METHOD = "login";
    private static final String LOGOUT_METHOD = "logout";
    private static final String CREATE_SESSION_METHOD = "createSession";
    private static final String GET_SESSION_METHOD = "getSession";

    @Override
    public void initEventHandler() {
        //empty
    }

    @Override
    public String getChannelName() {
        return CHANNEL_NAME;
    }

    @Override
    public MethodChannel.MethodCallHandler getMethodHandler() {
        return this::handleMethod;
    }

    @Override
    public void handleMethod(MethodCall methodCall, MethodChannel.Result result) {
        Map<String, Object> data = methodCall.arguments();
        switch (methodCall.method) {
            case LOGIN_METHOD:
                login(data, result);
                break;
            case LOGOUT_METHOD:
                logout(result);
                break;
            case CREATE_SESSION_METHOD:
                createSession(data, result);
                break;
            case GET_SESSION_METHOD:
                getSession(result);
        }
    }

    private void login(Map<String, Object> data, final MethodChannel.Result result) {
        String login = data != null && data.containsKey("login") ? (String) data.get("login") : null;
        String password = data != null && data.containsKey("password") ? (String) data.get("password") : null;

        QBUsers.signIn(login, password).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Map<String, Object> map = new HashMap<>();

                Map<String, Object> user = AuthMapper.qbUserToMap(qbUser);
                map.put("user", user);

                QBSessionManager.getInstance().getActiveSession().setUserId(qbUser.getId());
                QBSession qbSession = QBSessionManager.getInstance().getActiveSession();
                Map<String, Object> session = AuthMapper.qbSessionToMap(qbSession);
                map.put("session", session);

                result.success(map);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void logout(final MethodChannel.Result result) {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void createSession(Map<String, Object> data, final MethodChannel.Result result) {
        String token = data != null && data.containsKey("token") ? (String) data.get("token") : null;
        String tokenExpirationDate = data != null && data.containsKey("tokenExpirationDate") ?
                (String) data.get("tokenExpirationDate") : null;

        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(tokenExpirationDate)) {
            result.error("The parameters are wrong", null, null);
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date expirationDate;

        try {
            expirationDate = simpleDateFormat.parse(tokenExpirationDate);
        } catch (ParseException e) {
            e.printStackTrace();
            result.error("Parameter tokenExpirationDate is wrong", null, null);
            return;
        }

        QBSessionManager.getInstance().createActiveSession(token, expirationDate);
        QBSession qbSession = QBSessionManager.getInstance().getActiveSession();
        Map<String, Object> session = AuthMapper.qbSessionToMap(qbSession);
        result.success(session);
    }

    private void getSession(final MethodChannel.Result result) {
        QBSession qbSession = QBSessionManager.getInstance().getActiveSession();
        Map<String, Object> session = AuthMapper.qbSessionToMap(qbSession);
        result.success(session);
    }
}