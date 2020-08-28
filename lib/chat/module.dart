import 'dart:async';
import 'dart:collection';

import 'package:flutter/services.dart';
import 'package:quickblox_sdk/mappers/qb_attachment_mapper.dart';
import 'package:quickblox_sdk/mappers/qb_dialog_mapper.dart';
import 'package:quickblox_sdk/mappers/qb_filter_mapper.dart';
import 'package:quickblox_sdk/mappers/qb_message_mapper.dart';
import 'package:quickblox_sdk/mappers/qb_sort_mapper.dart';
import 'package:quickblox_sdk/models/qb_attachment.dart';
import 'package:quickblox_sdk/models/qb_dialog.dart';
import 'package:quickblox_sdk/models/qb_filter.dart';
import 'package:quickblox_sdk/models/qb_message.dart';
import 'package:quickblox_sdk/models/qb_sort.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class Chat {
  const Chat();

  ///////////////////////////////////////////////////////////////////////////
  // CHAT MODULE
  ///////////////////////////////////////////////////////////////////////////

  //Channel name
  static const CHANNEL_NAME = "FlutterQBChatChannel";

  //Methods
  static const CONNECT_METHOD = "connect";
  static const DISCONNECT_METHOD = "disconnect";
  static const IS_CONNECTED_METHOD = "isConnected";
  static const PING_SERVER_METHOD = "pingServer";
  static const PING_USER_METHOD = "pingUser";
  static const GET_DIALOGS_METHOD = "getDialogs";
  static const GET_DIALOGS_COUNT_METHOD = "getDialogsCount";
  static const UPDATE_DIALOG_METHOD = "updateDialog";
  static const CREATE_DIALOG_METHOD = "createDialog";
  static const DELETE_DIALOG_METHOD = "deleteDialog";
  static const LEAVE_DIALOG_METHOD = "leaveDialog";
  static const JOIN_DIALOG_METHOD = "joinDialog";
  static const GET_ONLINE_USERS_METHOD = "getOnlineUsers";
  static const SEND_MESSAGE_METHOD = "sendMessage";
  static const SEND_SYSTEM_MESSAGE_METHOD = "sendSystemMessage";
  static const MARK_MESSAGE_READ_METHOD = "markMessageRead";
  static const SEND_IS_TYPING_METHOD = "sendIsTyping";
  static const SEND_STOPPED_TYPING_METHOD = "sendStoppedTyping";
  static const MARK_MESSAGE_DELIVERED_METHOD = "markMessageDelivered";
  static const GET_DIALOG_MESSAGES_METHOD = "getDialogMessages";

  //Module
  static const _chatModule = const MethodChannel(CHANNEL_NAME);

  Future<void> connect(int userId, String password) async {
    Map<String, Object> data = Map();

    if (userId != null) {
      data["userId"] = userId;
    }
    if (password != null) {
      data["password"] = password;
    }

    await _chatModule.invokeMethod(CONNECT_METHOD, data);
  }

  Future<void> disconnect() async {
    await _chatModule.invokeMethod(DISCONNECT_METHOD);
  }

  Future<bool> isConnected() async {
    return _chatModule.invokeMethod(IS_CONNECTED_METHOD);
  }

  Future<void> pingServer() async {
    await _chatModule.invokeMethod(PING_SERVER_METHOD);
  }

  Future<void> pingUser(int userId) async {
    Map<String, Object> data = Map();

    if (userId != null) {
      data["userId"] = userId;
    }

    await _chatModule.invokeMethod(PING_USER_METHOD, data);
  }

  Future<List<QBDialog>> getDialogs(
      {QBSort sort, QBFilter filter, int limit, int skip}) async {
    Map<String, Object> data = new Map();

    if (sort != null) {
      data["sort"] = QBSortMapper.sortToMap(sort);
    }
    if (filter != null) {
      data["filter"] = QBFilterMapper.filterToMap(filter);
    }
    if (limit != null) {
      data["limit"] = limit;
    }
    if (skip != null) {
      data["skip"] = skip;
    }
    LinkedHashMap<dynamic, dynamic> dialogsHashMap =
        await _chatModule.invokeMethod(GET_DIALOGS_METHOD, data);

    Map<String, Object> dialogsMap =
        new Map<String, Object>.from(dialogsHashMap);

    List<Object> list = dialogsMap["dialogs"];

    List<QBDialog> dialogList = new List();

    for (final dialogMap in list) {
      Map<String, Object> dialogHashMap =
          new Map<String, Object>.from(dialogMap);
      QBDialog qbDialog = QBDialogMapper.mapToQBDialog(dialogHashMap);
      dialogList.add(qbDialog);
    }

    return dialogList;
  }

  Future<int> getDialogsCount({QBFilter qbFilter, int limit, int skip}) async {
    Map<String, Object> data = new Map();

    if (qbFilter != null) {
      data["filter"] = QBFilterMapper.filterToMap(qbFilter);
    }
    if (limit != null) {
      data["limit"] = limit;
    }
    if (skip != null) {
      data["skip"] = skip;
    }

    return await _chatModule.invokeMethod(GET_DIALOGS_COUNT_METHOD, data);
  }

  Future<QBDialog> updateDialog(String dialogId,
      {List<int> addUsers, List<int> removeUsers, String dialogName}) async {
    Map<String, Object> data = new Map();

    if (addUsers != null) {
      data["addUsers"] = addUsers;
    }
    if (removeUsers != null) {
      data["removeUsers"] = removeUsers;
    }
    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }
    if (dialogName != null) {
      data["dialogName"] = dialogName;
    }

    Map<String, Object> map = new Map<String, dynamic>.from(
        await _chatModule.invokeMethod(UPDATE_DIALOG_METHOD, data));

    QBDialog updatedDialog = QBDialogMapper.mapToQBDialog(map);

    return updatedDialog;
  }

  Future<QBDialog> createDialog(List<int> occupantsIds, String dialogName,
      {int dialogType}) async {
    Map<String, Object> data = new Map();

    if (occupantsIds != null) {
      data["occupantsIds"] = occupantsIds;
    }
    if (dialogName != null) {
      data["name"] = dialogName;
    }
    if (dialogType != null) {
      data["dialogType"] = dialogType;
    }
    Map<String, Object> map = new Map<String, dynamic>.from(
        await _chatModule.invokeMethod(CREATE_DIALOG_METHOD, data));

    QBDialog createdDialog = QBDialogMapper.mapToQBDialog(map);
    return createdDialog;
  }

  Future<void> deleteDialog(String dialogId) async {
    Map<String, Object> data = new Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }

    await _chatModule.invokeMethod(DELETE_DIALOG_METHOD, data);
  }

  Future<void> leaveDialog(String dialogId) async {
    Map<String, Object> data = new Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }

    await _chatModule.invokeMethod(LEAVE_DIALOG_METHOD, data);
  }

  Future<void> joinDialog(String dialogId) async {
    Map<String, Object> data = new Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }

    await _chatModule.invokeMethod(JOIN_DIALOG_METHOD, data);
  }

  Future<dynamic> getOnlineUsers(String dialogId) async {
    Map<String, Object> data = new Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }

    List<dynamic> onlineUsers =
        await _chatModule.invokeMethod(GET_ONLINE_USERS_METHOD, data);
    return onlineUsers;
  }

  Future<void> sendMessage(String dialogId,
      {String body,
      List<QBAttachment> attachments,
      Map<String, Object> properties,
      bool markable = false,
      String dateSent,
      bool saveToHistory}) async {
    Map<String, Object> data = Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }
    if (body != null) {
      data["body"] = body;
    }
    if (attachments != null && attachments.length > 0) {
      List<Map<String, Object>> attachmentMapList = List();
      for (final attachment in attachments) {
        Map<String, Object> attachmentMap =
            QBAttachmentMapper.qbAttachmentToMap(attachment);
        attachmentMapList.add(attachmentMap);
      }
      data["attachments"] = attachmentMapList;
    }
    if (properties != null && properties.length > 0) {
      data["properties"] = properties;
    }
    if (markable != null) {
      data["markable"] = markable;
    }
    if (dateSent != null) {
      data["dateSent"] = dateSent;
    }
    if (saveToHistory != null) {
      data["saveToHistory"] = saveToHistory;
    }
    await _chatModule.invokeMethod(SEND_MESSAGE_METHOD, data);
  }

  Future<void> sendSystemMessage(int recipientId,
      {Map<String, Object> properties}) async {
    Map<String, Object> data = Map();
    if (recipientId != null) {
      data["recipientId"] = recipientId;
    }
    if (properties != null) {
      data["properties"] = properties;
    }
    await _chatModule.invokeMethod(SEND_SYSTEM_MESSAGE_METHOD, data);
  }

  Future<void> markMessageRead(QBMessage qbMessage) async {
    Map<String, Object> data = Map();

    if (qbMessage != null) {
      Map<String, Object> messageMap =
          QBMessageMapper.qbMessageToMap(qbMessage);
      data["message"] = messageMap;
    }

    await _chatModule.invokeMethod(MARK_MESSAGE_READ_METHOD, data);
  }

  Future<void> markMessageDelivered(QBMessage qbMessage) async {
    Map<String, Object> data = Map();

    if (qbMessage != null) {
      Map<String, Object> messageMap =
          QBMessageMapper.qbMessageToMap(qbMessage);
      data["message"] = messageMap;
    }
    await _chatModule.invokeMethod(MARK_MESSAGE_DELIVERED_METHOD, data);
  }

  Future<void> sendIsTyping(String dialogId) async {
    Map<String, Object> data = Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }

    await _chatModule.invokeMethod(SEND_IS_TYPING_METHOD, data);
  }

  Future<void> sendStoppedTyping(String dialogId) async {
    Map<String, Object> data = Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }

    await _chatModule.invokeMethod(SEND_STOPPED_TYPING_METHOD, data);
  }

  Future<List<QBMessage>> getDialogMessages(String dialogId,
      {QBSort sort,
      QBFilter filter,
      int limit = 100,
      int skip = 0,
      bool markAsRead = false}) async {
    Map<String, Object> data = Map();

    if (dialogId != null) {
      data["dialogId"] = dialogId;
    }
    if (sort != null) {
      data["sort"] = QBSortMapper.sortToMap(sort);
    }
    if (filter != null) {
      data["filter"] = QBFilterMapper.filterToMap(filter);
    }
    if (limit != null) {
      data["limit"] = limit;
    }
    if (skip != null) {
      data["skip"] = skip;
    }
    if (markAsRead != null) {
      data["markAsRead"] = markAsRead;
    }
    LinkedHashMap<dynamic, dynamic> messagesHashMap =
        await _chatModule.invokeMethod(GET_DIALOG_MESSAGES_METHOD, data);

    Map<String, Object> messagesMap =
        new Map<String, Object>.from(messagesHashMap);

    List<Object> list = messagesMap["messages"];

    List<QBMessage> messagesList = new List();

    for (final messageMap in list) {
      Map<String, Object> messageHashMap =
          new Map<String, Object>.from(messageMap);
      QBMessage qbMessage = QBMessageMapper.mapToQBDialog(messageHashMap);
      messagesList.add(qbMessage);
    }

    return messagesList;
  }

  Future<StreamSubscription<dynamic>> subscribeChatEvent(
      String eventName, eventMethod,
      {onErrorMethod}) async {
    return EventChannel(eventName)
        .receiveBroadcastStream(eventName)
        .listen(eventMethod, onError: onErrorMethod);
  }
}
