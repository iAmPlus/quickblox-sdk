import 'dart:async';

import 'package:flutter/services.dart';
import 'package:quickblox_sdk/mappers/qb_event_mapper.dart';
import 'package:quickblox_sdk/models/qb_event.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class Notifications {
  const Notifications();

  ///////////////////////////////////////////////////////////////////////////
  // NOTIFICATIONS MODULE
  ///////////////////////////////////////////////////////////////////////////

  //Channel name
  static const CHANNEL_NAME = "FlutterQBNotificationEventsChannel";

  //Methods
  static const CREATE_METHOD = "create";
  static const UPDATE_METHOD = "update";
  static const REMOVE_METHOD = "remove";
  static const GET_BY_ID_METHOD = "getById";
  static const GET_METHOD = "get";

  //Module
  static const _notificationModule = const MethodChannel(CHANNEL_NAME);

  Future<List<QBEvent>> create(String type, String notificationType,
      int senderId, Map<String, Object> payload,
      {int id,
      bool active,
      String name,
      int pushType,
      double date,
      int endDate,
      String period,
      int occuredCount,
      List<String> recipientsIds,
      List<String> recipientsTagsAny,
      List<String> recipientsTagsAll,
      List<String> recipientsTagsExclude}) async {
    Map<String, Object> data = new Map();

    if (type != null) {
      data["type"] = type;
    }
    if (notificationType != null) {
      data["notificationType"] = notificationType;
    }
    if (senderId != null) {
      data["senderId"] = senderId;
    }
    if (id != null) {
      data["id"] = id;
    }
    if (active != null) {
      data["active"] = active;
    }
    if (name != null) {
      data["name"] = name;
    }
    if (pushType != null) {
      data["pushType"] = pushType;
    }
    if (date != null) {
      data["date"] = date;
    }
    if (endDate != null) {
      data["endDate"] = endDate;
    }
    if (period != null) {
      data["period"] = period;
    }
    if (occuredCount != null) {
      data["occuredCount"] = occuredCount;
    }
    if (recipientsIds != null) {
      data["recipientsIds"] = recipientsIds;
    }
    if (recipientsTagsAny != null) {
      data["recipientsTagsAny"] = recipientsTagsAny;
    }
    if (recipientsTagsAll != null) {
      data["recipientsTagsAll"] = recipientsTagsAll;
    }
    if (recipientsTagsExclude != null) {
      data["recipientsTagsExclude"] = recipientsTagsExclude;
    }
    if (payload != null) {
      data["payload"] = payload;
    }

    List<Object> list =
        await _notificationModule.invokeMethod(CREATE_METHOD, data);

    List<QBEvent> eventsList = new List();
    for (final item in list) {
      Map<String, Object> eventMap = Map.castFrom(item);
      QBEvent event = QBEventMapper.mapToQBEvent(eventMap);
      eventsList.add(event);
    }

    return eventsList;
  }

  Future<QBEvent> update(int id,
      {bool active,
      Map<String, Object> payload,
      double date,
      String period,
      String name}) async {
    Map<String, Object> data = new Map();

    if (id != null) {
      data["id"] = id;
    }
    if (active != null) {
      data["active"] = active;
    }
    if (payload != null) {
      data["payload"] = payload;
    }
    if (date != null) {
      data["date"] = date;
    }
    if (period != null) {
      data["period"] = period;
    }
    if (name != null) {
      data["name"] = name;
    }

    Object object = new Map<String, dynamic>.from(
        await _notificationModule.invokeMethod(UPDATE_METHOD, data));

    Map<String, Object> eventMap = Map.castFrom(object);
    QBEvent qbEvent = QBEventMapper.mapToQBEvent(eventMap);

    return qbEvent;
  }

  Future<void> remove(int id) async {
    Map<String, Object> data = new Map();

    if (id != null) {
      data["id"] = id;
    }

    await _notificationModule.invokeMethod(REMOVE_METHOD, data);
  }

  Future<QBEvent> getById(int id) async {
    Map<String, Object> data = new Map();
    if (id != null) {
      data["id"] = id;
    }

    Object object = new Map<String, dynamic>.from(
        await _notificationModule.invokeMethod(GET_BY_ID_METHOD, data));
    Map<String, Object> eventMap = Map.castFrom(object);
    QBEvent qbEvent = QBEventMapper.mapToQBEvent(eventMap);

    return qbEvent;
  }

  Future<List<QBEvent>> get({int page = 1, int perPage = 10}) async {
    Map<String, Object> data = new Map();
    if (page != null) {
      data["page"] = page;
    }

    if (perPage != null) {
      data["perPage"] = perPage;
    }

    List<Object> list =
        await _notificationModule.invokeMethod(GET_METHOD, data);

    List<QBEvent> eventsList = new List();
    for (final item in list) {
      Map<String, Object> eventMap = Map.castFrom(item);
      QBEvent event = QBEventMapper.mapToQBEvent(eventMap);
      eventsList.add(event);
    }

    return eventsList;
  }
}
