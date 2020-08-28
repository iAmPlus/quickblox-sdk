import 'package:quickblox_sdk/models/qb_attachment.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBAttachmentMapper {
  static QBAttachment mapToQBAttachment(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBAttachment qbAttachment = new QBAttachment();

    if (map.containsKey("type") && map["type"] != null) {
      qbAttachment.type = map["type"];
    }
    if (map.containsKey("id") && map["id"] != null) {
      qbAttachment.id = map["id"];
    }
    if (map.containsKey("url") && map["url"] != null) {
      qbAttachment.url = map["url"];
    }
    if (map.containsKey("name") && map["name"] != null) {
      qbAttachment.name = map["name"];
    }
    if (map.containsKey("contentType") && map["contentType"] != null) {
      qbAttachment.contentType = map["contentType"];
    }
    if (map.containsKey("data") && map["data"] != null) {
      qbAttachment.data = map["data"];
    }
    if (map.containsKey("size") && map["size"] != null) {
      qbAttachment.size = map["size"];
    }
    if (map.containsKey("height") && map["height"] != null) {
      qbAttachment.height = map["height"];
    }
    if (map.containsKey("width") && map["width"] != null) {
      qbAttachment.width = map["width"];
    }
    if (map.containsKey("duration") && map["duration"] != null) {
      qbAttachment.duration = map["duration"];
    }

    return qbAttachment;
  }

  static Map<String, Object> qbAttachmentToMap(QBAttachment qbAttachment) {
    if (qbAttachment == null) {
      return null;
    }

    Map<String, Object> map = new Map();

    if (qbAttachment.type != null) {
      map["type"] = qbAttachment.type;
    }
    if (qbAttachment.id != null) {
      map["id"] = qbAttachment.id;
    }
    if (qbAttachment.url != null) {
      map["url"] = qbAttachment.url;
    }
    if (qbAttachment.name != null) {
      map["name"] = qbAttachment.name;
    }
    if (qbAttachment.contentType != null) {
      map["contentType"] = qbAttachment.contentType;
    }
    if (qbAttachment.data != null) {
      map["data"] = qbAttachment.data;
    }
    if (qbAttachment.size != null) {
      map["size"] = qbAttachment.size;
    }
    if (qbAttachment.height != null) {
      map["height"] = qbAttachment.height;
    }
    if (qbAttachment.width != null) {
      map["width"] = qbAttachment.width;
    }
    if (qbAttachment.duration != null) {
      map["duration"] = qbAttachment.duration;
    }

    return map;
  }
}
