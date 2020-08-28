import 'dart:async';

import 'package:flutter/services.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class Settings {
  const Settings();

  ///////////////////////////////////////////////////////////////////////////
  // SETTINGS MODULE
  ///////////////////////////////////////////////////////////////////////////

  //Channel name
  static const CHANNEL_NAME = "FlutterQBSettingsChannel";

  //Methods
  static const INIT_METHOD = "init";
  static const GET_METHOD = "get";
  static const ENABLE_CARBONS_METHOD = "enableCarbons";
  static const DISABLE_CARBONS_METHOD = "disableCarbons";
  static const INIT_STREAM_MANAGEMENT_METHOD = "initStreamManagement";
  static const ENABLE_AUTO_RECONNECT_METHOD = "enableAutoReconnect";

  //Module
  static const _settingsModule = const MethodChannel(CHANNEL_NAME);

  Future<void> init(
      String appId, String authKey, String authSecret, String accountKey,
      {String apiEndpoint, String chatEndpoint}) async {
    Map<String, Object> data = new Map();

    if (appId != null) {
      data["appId"] = appId;
    }
    if (authKey != null) {
      data["authKey"] = authKey;
    }
    if (authSecret != null) {
      data["authSecret"] = authSecret;
    }
    if (accountKey != null) {
      data["accountKey"] = accountKey;
    }
    if (apiEndpoint != null) {
      data["apiEndpoint"] = apiEndpoint;
    }
    if (chatEndpoint != null) {
      data["chatEndpoint"] = chatEndpoint;
    }
    await _settingsModule.invokeMethod(INIT_METHOD, data);
  }

  Future<Map<String, Object>> get() async {
    Map<String, Object> map = new Map<String, dynamic>.from(
        await _settingsModule.invokeMethod(GET_METHOD));
    return map;
  }

  Future<void> enableCarbons() async {
    await _settingsModule.invokeMethod(ENABLE_CARBONS_METHOD);
  }

  Future<void> disableCarbons() async {
    await _settingsModule.invokeMethod(DISABLE_CARBONS_METHOD);
  }

  Future<void> initStreamManagement(int messageTimeout,
      {bool autoReconnect}) async {
    Map<String, Object> data = new Map();
    if (messageTimeout != null) {
      data["messageTimeout"] = messageTimeout;
    }
    if (autoReconnect != null) {
      data["autoReconnect"] = autoReconnect;
    }

    await _settingsModule.invokeMethod(INIT_STREAM_MANAGEMENT_METHOD, data);
  }

  Future<void> enableAutoReconnect(bool enable) async {
    Map<String, Object> data = new Map();

    if (enable != null) {
      data["enable"] = enable;
    }

    await _settingsModule.invokeMethod(ENABLE_AUTO_RECONNECT_METHOD, data);
  }
}
