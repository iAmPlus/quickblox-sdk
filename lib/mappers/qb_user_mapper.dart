import 'package:quickblox_sdk/models/qb_user.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBUserMapper {
  static QBUser mapToQBUser(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBUser qbUser = new QBUser();

    if (map.containsKey("blobId")) {
      qbUser.blobId = map["blobId"];
    }
    if (map.containsKey("customData")) {
      qbUser.customData = map["customData"];
    }
    if (map.containsKey("email")) {
      qbUser.email = map["email"];
    }
    if (map.containsKey("externalId")) {
      qbUser.externalId = map["externalId"];
    }
    if (map.containsKey("facebookId")) {
      qbUser.facebookId = map["facebookId"];
    }
    if (map.containsKey("fullName")) {
      qbUser.fullName = map["fullName"];
    }
    if (map.containsKey("id")) {
      qbUser.id = map["id"];
    }
    if (map.containsKey("login")) {
      qbUser.login = map["login"];
    }
    if (map.containsKey("phone")) {
      qbUser.phone = map["phone"];
    }
    if (map.containsKey("tags")) {
      List<String> tagsList = List.from(map["tags"]);
      qbUser.tags = tagsList;
    }
    if (map.containsKey("twitterId")) {
      qbUser.twitterId = map["twitterId"];
    }
    if (map.containsKey("website")) {
      qbUser.website = map["website"];
    }
    if (map.containsKey("lastRequestAt")) {
      qbUser.lastRequestAt = map["lastRequestAt"];
    }

    return qbUser;
  }
}
