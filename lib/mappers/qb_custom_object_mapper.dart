import 'package:quickblox_sdk/models/qb_custom_object.dart';
import 'package:quickblox_sdk/models/qb_custom_object_permission.dart';
import 'package:quickblox_sdk/models/qb_custom_object_permission_level.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBCustomObjectMapper {
  static QBCustomObject mapToQBCustomObject(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBCustomObject customObject = new QBCustomObject();
    QBCustomObjectPermission permission = new QBCustomObjectPermission();
    QBCustomObjectPermissionLevel permissionLevel =
        new QBCustomObjectPermissionLevel();

    if (map.containsKey("id")) {
      customObject.id = map["id"];
    }
    if (map.containsKey("parentId")) {
      customObject.parentId = map["parentId"];
    }
    if (map.containsKey("createdAt")) {
      customObject.createdAt = map["createdAt"];
    }
    if (map.containsKey("updatedAt")) {
      customObject.updatedAt = map["updatedAt"];
    }
    if (map.containsKey("className")) {
      customObject.className = map["className"];
    }
    if (map.containsKey("userId")) {
      customObject.userId = map["userId"];
    }
    if (map.containsKey("fields")) {
      Map<String, Object> fieldsMap = Map.from(map["fields"]);
      customObject.fields = fieldsMap;
    }
    if (map.containsKey("permission")) {
      Map<String, Object> permissionMap = Map.from(map["permission"]);
      QBCustomObjectPermission permission =
          mapToQBCustomObjectPermission(permissionMap);
      customObject.permission = permission;
    }

    return customObject;
  }

  static QBCustomObjectPermission mapToQBCustomObjectPermission(
      Map<String, Object> map) {
    QBCustomObjectPermission permission = new QBCustomObjectPermission();

    if (map.containsKey("customObjectId")) {
      permission.customObjectId = map["customObjectId"];
    }
    if (map.containsKey("readLevel")) {
      Map<String, Object> permissionMap = Map.from(map["readLevel"]);
      QBCustomObjectPermissionLevel permissionLevel =
          mapToQBCustomObjectPermissionLevel(permissionMap);
      permission.readLevel = permissionLevel;
    }
    if (map.containsKey("updateLevel")) {
      Map<String, Object> permissionMap = Map.from(map["updateLevel"]);
      QBCustomObjectPermissionLevel permissionLevel =
          mapToQBCustomObjectPermissionLevel(permissionMap);
      permission.updateLevel = permissionLevel;
    }
    if (map.containsKey("deleteLevel")) {
      Map<String, Object> permissionMap = Map.from(map["deleteLevel"]);
      QBCustomObjectPermissionLevel permissionLevel =
          mapToQBCustomObjectPermissionLevel(permissionMap);
      permission.deleteLevel = permissionLevel;
    }

    return permission;
  }

  static QBCustomObjectPermissionLevel mapToQBCustomObjectPermissionLevel(
      Map<String, Object> map) {
    QBCustomObjectPermissionLevel permissionLevel =
        new QBCustomObjectPermissionLevel();

    if (map.containsKey("access")) {
      permissionLevel.access = map["access"];
    }
    if (map.containsKey("usersIds")) {
      permissionLevel.usersIds = map["usersIds"];
    }
    if (map.containsKey("usersGroups")) {
      permissionLevel.usersGroups = map["usersGroups"];
    }

    return permissionLevel;
  }
}
