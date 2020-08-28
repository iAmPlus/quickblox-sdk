import 'package:quickblox_sdk/models/qb_session.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBSessionMapper {
  static QBSession mapToQBSession(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBSession qbSession = new QBSession();

    if (map.containsKey("token")) {
      qbSession.token = map["token"];
    }

    if (map.containsKey("tokenExpirationDate")) {
      qbSession.tokenExpirationDate = map["tokenExpirationDate"];
    }

    return qbSession;
  }

  static Map<String, Object> qbSessionToMap(QBSession session) {
    Map<String, Object> map = new Map();

    if (session == null) {
      return map;
    }

    if (session.token != null && session.token.isNotEmpty) {
      map["token"] = session.token;
    }
    if (session.tokenExpirationDate != null &&
        session.tokenExpirationDate.isNotEmpty) {
      map["tokenExpirationDate"] = session.tokenExpirationDate;
    }

    return map;
  }
}
