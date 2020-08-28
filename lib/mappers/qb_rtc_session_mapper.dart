import 'package:quickblox_sdk/models/qb_rtc_session.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBRTCSessionMapper {
  static QBRTCSession mapToQBRtcSession(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBRTCSession qbrtcSession = new QBRTCSession();

    if (map.containsKey("id")) {
      qbrtcSession.id = map["id"];
    }
    if (map.containsKey("type")) {
      qbrtcSession.type = map["type"];
    }
    if (map.containsKey("state")) {
      qbrtcSession.state = map["state"];
    }
    if (map.containsKey("initiatorId")) {
      qbrtcSession.initiatorId = map["initiatorId"];
    }
    if (map.containsKey("opponentsIds")) {
      List<int> opponentIdsList = List.from(map["opponentsIds"]);
      qbrtcSession.opponentsIds = opponentIdsList;
    }

    return qbrtcSession;
  }
}
