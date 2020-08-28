import 'package:quickblox_sdk/models/qb_dialog.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBDialogMapper {
  static QBDialog mapToQBDialog(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBDialog qbDialog = new QBDialog();

    if (map.containsKey("isJoined")) {
      qbDialog.isJoined = map["isJoined"];
    }
    if (map.containsKey("createdAt")) {
      qbDialog.createdAt = map["createdAt"];
    }
    if (map.containsKey("lastMessage")) {
      qbDialog.lastMessage = map["lastMessage"];
    }
    if (map.containsKey("lastMessageDateSent")) {
      qbDialog.lastMessageDateSent = map["lastMessageDateSent"];
    }
    if (map.containsKey("lastMessageUserId")) {
      qbDialog.lastMessageUserId = map["lastMessageUserId"];
    }
    if (map.containsKey("name")) {
      qbDialog.name = map["name"];
    }
    if (map.containsKey("photo")) {
      qbDialog.photo = map["photo"];
    }
    if (map.containsKey("type")) {
      qbDialog.type = map["type"];
    }
    if (map.containsKey("unreadMessagesCount")) {
      qbDialog.unreadMessagesCount = map["unreadMessagesCount"];
    }
    if (map.containsKey("updatedAt")) {
      qbDialog.updatedAt = map["updatedAt"];
    }
    if (map.containsKey("userId")) {
      qbDialog.userId = map["userId"];
    }
    if (map.containsKey("roomJid")) {
      qbDialog.roomJid = map["roomJid"];
    }
    if (map.containsKey("id")) {
      qbDialog.id = map["id"];
    }
    if (map.containsKey("occupantsIds")) {
      List<Object> list = map["occupantsIds"];
      qbDialog.occupantsIds = list.cast<int>();
    }
    if (map.containsKey("customData")) {
      qbDialog.customData = map["customData"];
    }

    return qbDialog;
  }

  static Map<String, Object> qbChatDialogToMap(QBDialog dialog) {
    Map<String, Object> map = new Map();

    if (dialog == null) {
      return map;
    }

    map["isJoined"] = dialog.isJoined;
    map["createdAt"] = dialog.createdAt;
    map["lastMessage"] = dialog.lastMessage;
    map["lastMessageDateSent"] = dialog.lastMessageDateSent;
    map["lastMessageUserId"] = dialog.lastMessageUserId;
    map["name"] = dialog.name;
    map["photo"] = dialog.photo;
    map["type"] = dialog.type;
    map["unreadMessagesCount"] = dialog.unreadMessagesCount;
    map["updatedAt"] = dialog.updatedAt;
    map["userId"] = dialog.userId;
    map["roomJid"] = dialog.roomJid;
    map["id"] = dialog.id;
    map["occupantsIds"] = dialog.occupantsIds;

    // TODO: 2019-10-21 need to check this logic
    /*if (dialog.getCustomData() != null &&
        !TextUtils.isEmpty(dialog.getCustomData().getClassName())) {
      Map customDataMap = qbDialogCustomDataToMap(dialog.getCustomData());
      String className = dialog.getCustomData().getClassName();
      Map<String, Object> data = new HashMap<>();
      data.put(className, customDataMap);
      map.put("customData", data);
    }*/

    return map;
  }
}
