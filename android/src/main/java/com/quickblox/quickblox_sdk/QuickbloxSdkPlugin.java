package com.quickblox.quickblox_sdk;

import com.quickblox.quickblox_sdk.auth.AuthModule;
import com.quickblox.quickblox_sdk.chat.ChatModule;
import com.quickblox.quickblox_sdk.customobjects.CustomObjectsModule;
import com.quickblox.quickblox_sdk.file.FileModule;
import com.quickblox.quickblox_sdk.notification.NotificationModule;
import com.quickblox.quickblox_sdk.push.PushModule;
import com.quickblox.quickblox_sdk.settings.SettingsModule;
import com.quickblox.quickblox_sdk.users.UsersModule;
import com.quickblox.quickblox_sdk.webrtc.QBWebRTCViewFactory;
import com.quickblox.quickblox_sdk.webrtc.WebRTCCallService;
import com.quickblox.quickblox_sdk.webrtc.WebRTCModule;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class QuickbloxSdkPlugin {
    private static final String VIEW_TYPE_ID = "QBWebRTCViewFactory";

    //Modules
    private AuthModule authModule;
    private SettingsModule settingsModule;
    private UsersModule usersModule;
    private ChatModule chatModule;
    private CustomObjectsModule customObjectsModule;
    private FileModule fileModule;
    private NotificationModule notificationModule;
    private PushModule pushModule;
    private WebRTCModule webRTCModule;

    //View Factory
    private QBWebRTCViewFactory qbWebRTCViewFactory;

    private static QuickbloxSdkPlugin sdkPlugin;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        initPlugin();
        sdkPlugin.initModules(registrar);
        sdkPlugin.initViewsFactory(registrar);
    }

    private static synchronized void initPlugin() {
        if (sdkPlugin == null) {
            sdkPlugin = new QuickbloxSdkPlugin();
        }
    }

    private void initModules(Registrar registrar) {
        //AuthModule
        authModule = new AuthModule();
        MethodChannel authChannel = new MethodChannel(registrar.messenger(), authModule.getChannelName());
        authChannel.setMethodCallHandler(authModule.getMethodHandler());

        //SettingsModule
        settingsModule = new SettingsModule(registrar.context(), new ModuleSettingsEvents());
        MethodChannel settingsChannel = new MethodChannel(registrar.messenger(), settingsModule.getChannelName());
        settingsChannel.setMethodCallHandler(settingsModule.getMethodHandler());

        //UsersModule
        usersModule = new UsersModule();
        MethodChannel usersChannel = new MethodChannel(registrar.messenger(), usersModule.getChannelName());
        usersChannel.setMethodCallHandler(usersModule.getMethodHandler());

        //Chat Module
        chatModule = new ChatModule(registrar.messenger(), new ModuleSettingsEvents());
        MethodChannel chatChannel = new MethodChannel(registrar.messenger(), chatModule.getChannelName());
        chatChannel.setMethodCallHandler(chatModule.getMethodHandler());

        //Custom Objects Module
        customObjectsModule = new CustomObjectsModule();
        MethodChannel customObjectsChannel = new MethodChannel(registrar.messenger(), customObjectsModule.getChannelName());
        customObjectsChannel.setMethodCallHandler(customObjectsModule.getMethodHandler());

        //File Module
        fileModule = new FileModule(registrar.context(), registrar.messenger());
        MethodChannel fileChannel = new MethodChannel(registrar.messenger(), fileModule.getChannelName());
        fileChannel.setMethodCallHandler(fileModule.getMethodHandler());

        //Notification Module
        notificationModule = new NotificationModule();
        MethodChannel notificationChannel = new MethodChannel(registrar.messenger(), notificationModule.getChannelName());
        notificationChannel.setMethodCallHandler(notificationModule.getMethodHandler());

        //Push Module
        pushModule = new PushModule(registrar.context());
        MethodChannel pushChannel = new MethodChannel(registrar.messenger(), pushModule.getChannelName());
        pushChannel.setMethodCallHandler(pushModule.getMethodHandler());

        //WebRTC Module
        webRTCModule = new WebRTCModule(registrar.messenger(), registrar.context());
        MethodChannel webRTCChannel = new MethodChannel(registrar.messenger(), webRTCModule.getChannelName());
        webRTCChannel.setMethodCallHandler(webRTCModule.getMethodHandler());
    }

    private void initViewsFactory(Registrar registrar) {
        //View Factory
        qbWebRTCViewFactory = new QBWebRTCViewFactory(registrar.messenger());
        registrar.platformViewRegistry().registerViewFactory(VIEW_TYPE_ID, qbWebRTCViewFactory);
    }

    private class ModuleSettingsEvents implements ModuleEvents {

        @Override
        public void onInitCredentials() {
            chatModule.onInitCredentials();
        }

        @Override
        public void onChatConnected() {
        }

        @Override
        public void onChatDisconnected() {
        }

        @Override
        public void onServiceStarted(WebRTCCallService webRTCCallService) {
        }

        @Override
        public void onServiceReleased() {
        }
    }

    public interface ModuleEvents {
        void onInitCredentials();

        void onChatConnected();

        void onChatDisconnected();

        void onServiceStarted(WebRTCCallService webRTCCallService);

        void onServiceReleased();
    }
}