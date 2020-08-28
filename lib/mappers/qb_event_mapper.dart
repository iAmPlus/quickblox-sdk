import 'package:quickblox_sdk/models/qb_event.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBEventMapper {
  static QBEvent mapToQBEvent(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBEvent qbEvent = new QBEvent();

    if (map.containsKey("id")) {
      qbEvent.id = map["id"];
    }
    if (map.containsKey("name")) {
      qbEvent.name = map["name"];
    }
    if (map.containsKey("active")) {
      qbEvent.active = map["active"];
    }
    if (map.containsKey("notificationType")) {
      qbEvent.notificationType = map["notificationType"];
    }
    if (map.containsKey("pushType")) {
      qbEvent.pushType = map["pushType"];
    }
    if (map.containsKey("date")) {
      qbEvent.date = map["date"];
    }
    if (map.containsKey("endDate")) {
      qbEvent.endDate = map["endDate"];
    }
    if (map.containsKey("period")) {
      qbEvent.period = map["period"];
    }
    if (map.containsKey("occuredCount")) {
      qbEvent.occuredCount = map["occuredCount"];
    }
    if (map.containsKey("senderId")) {
      qbEvent.senderId = map["senderId"];
    }
    if (map.containsKey("recipientsIds")) {
      List<Object> objectsList = map["recipientsIds"];
      qbEvent.recipientsIds = objectsList.cast<String>();
    }
    if (map.containsKey("recipientsTagsAny")) {
      List<Object> objectsList = map["recipientsTagsAny"];
      qbEvent.recipientsTagsAny = objectsList.cast<String>();
    }
    if (map.containsKey("recipientsTagsAll")) {
      List<Object> objectsList = map["recipientsTagsAll"];
      qbEvent.recipientsTagsAll = objectsList.cast<String>();
    }
    if (map.containsKey("recipientsTagsExclude")) {
      List<Object> objectsList = map["recipientsTagsExclude"];
      qbEvent.recipientsTagsExclude = objectsList.cast<String>();
    }
    if (map.containsKey("payload")) {
      qbEvent.payload = map["payload"];
    }

    return qbEvent;
  }
}
