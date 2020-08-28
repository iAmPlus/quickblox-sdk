
package com.quickblox.quickblox_sdk.settings;

import android.content.Context;
import android.text.TextUtils;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric;
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder;
import com.quickblox.core.QBHttpConnectionConfig;
import com.quickblox.core.ServiceZone;
import com.quickblox.quickblox_sdk.BuildConfig;
import com.quickblox.quickblox_sdk.QuickbloxSdkPlugin;
import com.quickblox.quickblox_sdk.base.BaseModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class SettingsModule implements BaseModule {

    private static final String CHANNEL_NAME = "FlutterQBSettingsChannel";

    private static final String INIT_METHOD = "init";
    private static final String GET_METHOD = "get";
    private static final String ENABLE_CARBONS_METHOD = "enableCarbons";
    private static final String DISABLE_CARBONS_METHOD = "disableCarbons";
    private static final String INIT_STREAM_MANAGEMENT_METHOD = "initStreamManagement";
    private static final String ENABLE_AUTO_RECONNECT_METHOD = "enableAutoReconnect";

    private static final int HTTP_TIMEOUT_IN_SECONDS = 40 * 1000;

    private Context context;

    private QuickbloxSdkPlugin.ModuleEvents moduleEvents;

    public SettingsModule(@Nonnull Context context, QuickbloxSdkPlugin.ModuleEvents moduleEvents) {
        this.context = context;
        this.moduleEvents = moduleEvents;
    }

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
            case INIT_METHOD:
                init(data, result);
                break;
            case GET_METHOD:
                get(result);
                break;
            case ENABLE_CARBONS_METHOD:
                enableCarbons(result);
                break;
            case DISABLE_CARBONS_METHOD:
                disableCarbons(result);
                break;
            case INIT_STREAM_MANAGEMENT_METHOD:
                initStreamManagement(data, result);
                break;
            case ENABLE_AUTO_RECONNECT_METHOD:
                enableAutoReconnect(data, result);
                break;
        }
    }

    private void init(Map<String, Object> data, MethodChannel.Result result) {
        String applicationID = data != null && data.containsKey("appId") ? (String) data.get("appId") : null;
        String authKey = data != null && data.containsKey("authKey") ? (String) data.get("authKey") : null;
        String authSecret = data != null && data.containsKey("authSecret") ? (String) data.get("authSecret") : null;
        String accountKey = data != null && data.containsKey("accountKey") ? (String) data.get("accountKey") : null;
        String apiEndpoint = data != null && data.containsKey("apiEndpoint") ? (String) data.get("apiEndpoint") : null;
        String chatEndpoint = data != null && data.containsKey("chatEndpoint") ? (String) data.get("chatEndpoint") : null;

        if (TextUtils.isEmpty(applicationID) || TextUtils.isEmpty(authKey)
                || TextUtils.isEmpty(authSecret) || TextUtils.isEmpty(accountKey)) {
            result.error("The credentials are wrong", null, null);
        } else {
            QBSettings.getInstance().init(context, applicationID, authKey, authSecret);
            QBSettings.getInstance().setAccountKey(accountKey);

            if (!TextUtils.isEmpty(apiEndpoint) && !TextUtils.isEmpty(chatEndpoint)) {

                ServiceZone serviceZone = ServiceZone.PRODUCTION;
                if (BuildConfig.DEBUG) {
                    serviceZone = ServiceZone.DEVELOPMENT;
                }

                QBSettings.getInstance().setEndpoints(apiEndpoint, chatEndpoint, serviceZone);
                QBSettings.getInstance().setZone(serviceZone);
            }

            QBHttpConnectionConfig.setConnectTimeout(HTTP_TIMEOUT_IN_SECONDS);
            QBHttpConnectionConfig.setReadTimeout(HTTP_TIMEOUT_IN_SECONDS);

            moduleEvents.onInitCredentials();

            QBTcpConfigurationBuilder configurationBuilder = new QBTcpConfigurationBuilder()
                    .setAutojoinEnabled(true)
                    .setSocketTimeout(0);

            QBChatService.setConnectionFabric(new QBTcpChatConnectionFabric(configurationBuilder));

            QBChatService.setDebugEnabled(true);


            result.success(null);
        }
    }

    private void get(MethodChannel.Result result) {
        String applicationID = QBSettings.getInstance().getApplicationId();
        String authKey = QBSettings.getInstance().getAuthorizationKey();
        String authSecret = QBSettings.getInstance().getAuthorizationSecret();
        String accountKey = QBSettings.getInstance().getAccountKey();
        String apiEndpoint = QBSettings.getInstance().getApiEndpoint();
        String chatEndpoint = QBSettings.getInstance().getChatEndpoint();
        String sdkVersion = com.quickblox.BuildConfig.VERSION_NAME;

        Map<String, Object> data = new HashMap<>();
        data.put("appId", applicationID);
        data.put("authKey", authKey);
        data.put("authSecret", authSecret);
        data.put("accountKey", accountKey);
        data.put("apiEndpoint", apiEndpoint);
        data.put("chatEndpoint", chatEndpoint);
        data.put("sdkVersion", sdkVersion);

        result.success(data);
    }

    private void enableCarbons(final MethodChannel.Result result) {
        try {
            QBChatService.getInstance().enableCarbons();
            result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(e.getMessage(), null, null);
        }
    }

    private void disableCarbons(final MethodChannel.Result result) {
        try {
            QBChatService.getInstance().disableCarbons();
            result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(e.getMessage(), null, null);
        }
    }

    private void initStreamManagement(Map<String, Object> data, final MethodChannel.Result result) {
        boolean autoReconnect = data != null && data.containsKey("autoReconnect") && (boolean) data.get("autoReconnect");
        Integer messageTimeout = data != null && data.containsKey("messageTimeout") ? (Integer) data.get("messageTimeout") : null;

        if (messageTimeout != null && messageTimeout > 0) {
            QBChatService qbChatService = QBChatService.getInstance();
            qbChatService.setUseStreamManagement(true);
            qbChatService.setUseStreamManagementResumption(autoReconnect);
            qbChatService.setPreferredResumptionTime(messageTimeout);
            result.success(null);
        } else {
            result.error("The parameter messageTimeout is required", null, null);
        }
    }

    private void enableAutoReconnect(Map<String, Object> data, final MethodChannel.Result result) {
        boolean enabled = data != null && data.containsKey("enable") && (boolean) data.get("enable");

        QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();
        configurationBuilder.setReconnectionAllowed(enabled);
        QBChatService.setConfigurationBuilder(configurationBuilder);
        result.success(null);
    }
}