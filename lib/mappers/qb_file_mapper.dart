import 'package:quickblox_sdk/models/qb_file.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBFileMapper {
  static QBFile mapToQBFile(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBFile file = new QBFile();

    if (map.containsKey("id")) {
      file.id = map["id"];
    }
    if (map.containsKey("uid")) {
      file.uid = map["uid"];
    }
    if (map.containsKey("contentType")) {
      file.contentType = map["contentType"];
    }
    if (map.containsKey("name")) {
      file.name = map["name"];
    }
    if (map.containsKey("size")) {
      file.size = map["size"];
    }
    if (map.containsKey("completedAt")) {
      file.completedAt = map["completedAt"];
    }
    if (map.containsKey("isPublic")) {
      file.isPublic = map["isPublic"];
    }
    if (map.containsKey("lastReadAccessTime")) {
      file.lastReadAccessTime = map["lastReadAccessTime"];
    }
    if (map.containsKey("tags")) {
      file.tags = map["tags"];
    }

    return file;
  }
}
