import 'dart:async';

import 'package:flutter/services.dart';
import 'package:quickblox_sdk/mappers/qb_session_mapper.dart';
import 'package:quickblox_sdk/mappers/qb_user_mapper.dart';
import 'package:quickblox_sdk/models/qb_session.dart';
import 'package:quickblox_sdk/models/qb_user.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class Auth {
  const Auth();

  ///////////////////////////////////////////////////////////////////////////
  // AUTH MODULE
  ///////////////////////////////////////////////////////////////////////////

  //Channel name
  static const CHANNEL_NAME = "FlutterQBAuthChannel";

  //Methods
  static const LOGIN_METHOD = "login";
  static const LOGOUT_METHOD = "logout";
  static const CREATE_SESSION_METHOD = "createSession";
  static const GET_SESSION_METHOD = "getSession";

  //Module
  static const _authModule = const MethodChannel(CHANNEL_NAME);

  Future<QBLoginResult> login(String login, String password) async {
    Map<String, Object> data = Map();

    if (login != null) {
      data["login"] = login;
    }
    if (password != null) {
      data["password"] = password;
    }

    Map<String, Object> map = new Map<String, dynamic>.from(
        await _authModule.invokeMethod(LOGIN_METHOD, data));

    Map<String, Object> userMap = Map.from(map["user"]);
    Map<String, Object> sessionMap = Map.from(map["session"]);

    QBUser qbUser = QBUserMapper.mapToQBUser(userMap);
    QBSession qbSession = QBSessionMapper.mapToQBSession(sessionMap);

    QBLoginResult result = new QBLoginResult(qbUser, qbSession);

    return result;
  }

  Future<void> logout() async {
    await _authModule.invokeMethod(LOGOUT_METHOD);
  }

  Future<QBSession> createSession(QBSession qbSession) async {
    Map<String, Object> data = Map();

    if (qbSession != null) {
      data = QBSessionMapper.qbSessionToMap(qbSession);
    }

    Map<String, Object> resultMap = new Map<String, dynamic>.from(
        await _authModule.invokeMethod(CREATE_SESSION_METHOD, data));

    QBSession qbSessionResult = QBSessionMapper.mapToQBSession(resultMap);

    return qbSessionResult;
  }

  Future<QBSession> getSession() async {
    Map<String, Object> map = new Map<String, dynamic>.from(
        await _authModule.invokeMethod(GET_SESSION_METHOD));

    QBSession qbSession = QBSessionMapper.mapToQBSession(map);

    return qbSession;
  }
}

class QBLoginResult {
  QBLoginResult(this.qbUser, this.qbSession);

  QBUser qbUser;
  QBSession qbSession;
}
