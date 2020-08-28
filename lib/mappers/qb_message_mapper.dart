import 'dart:collection';

import 'package:quickblox_sdk/mappers/qb_attachment_mapper.dart';
import 'package:quickblox_sdk/models/qb_attachment.dart';
import 'package:quickblox_sdk/models/qb_message.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBMessageMapper {
  static QBMessage mapToQBDialog(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBMessage qbMessage = new QBMessage();

    if (map.containsKey("id")) {
      qbMessage.id = map["id"];
    }
    if (map.containsKey("attachments")) {
      List<Object> attachmentsMapsList = map["attachments"];

      List<QBAttachment> attachmentsList = new List();
      for (final attachmentDynamicMap in attachmentsMapsList) {
        Map<String, Object> attachmentMap =
            new Map<String, Object>.from(attachmentDynamicMap);
        QBAttachment qbAttachment =
            QBAttachmentMapper.mapToQBAttachment(attachmentMap);
        attachmentsList.add(qbAttachment);
      }
      qbMessage.attachments = attachmentsList;
    }
    if (map.containsKey("properties")) {
      LinkedHashMap<dynamic, dynamic> hashMap = map["properties"];

      Map<String, Object> propertiesMap =
          hashMap.map((key, value) => MapEntry(key as String, value as Object));

      qbMessage.properties = propertiesMap;
    }
    if (map.containsKey("dateSent")) {
      qbMessage.dateSent = map["dateSent"];
    }
    if (map.containsKey("senderId")) {
      qbMessage.senderId = map["senderId"];
    }
    if (map.containsKey("recipientId")) {
      qbMessage.recipientId = map["recipientId"];
    }
    if (map.containsKey("readIds")) {
      qbMessage.readIds = List.from(map["readIds"]);
    }
    if (map.containsKey("deliveredIds")) {
      qbMessage.deliveredIds = List.from(map["deliveredIds"]);
    }
    if (map.containsKey("dialogId")) {
      qbMessage.dialogId = map["dialogId"];
    }
    if (map.containsKey("markable")) {
      qbMessage.markable = map["markable"];
    }
    if (map.containsKey("delayed")) {
      qbMessage.delayed = map["delayed"];
    }
    if (map.containsKey("body")) {
      qbMessage.body = map["body"];
    }

    return qbMessage;
  }

  static Map<String, Object> qbMessageToMap(QBMessage qbMessage) {
    if (qbMessage == null) {
      return null;
    }

    Map<String, Object> messageMap = new Map();

    messageMap["id"] = qbMessage.id;

    List<QBAttachment> attachmentsList = qbMessage.attachments != null
        ? qbMessage.attachments != null
        : new List();
    List<Map<String, Object>> attachmentMapsList = new List();
    for (final qbAttachment in attachmentsList) {
      Map<String, Object> attachmentMap =
          QBAttachmentMapper.qbAttachmentToMap(qbAttachment);
      attachmentMapsList.add(attachmentMap);
    }

    messageMap["attachments"] = attachmentMapsList;
    messageMap["properties"] = qbMessage.properties;
    messageMap["dateSent"] = qbMessage.dateSent;
    messageMap["senderId"] = qbMessage.senderId;
    messageMap["recipientId"] = qbMessage.recipientId;
    messageMap["readIds"] = qbMessage.readIds;
    messageMap["deliveredIds"] = qbMessage.deliveredIds;
    messageMap["dialogId"] = qbMessage.dialogId;
    messageMap["markable"] = qbMessage.markable;
    messageMap["delayed"] = qbMessage.delayed;
    messageMap["body"] = qbMessage.body;

    return messageMap;
  }
}
