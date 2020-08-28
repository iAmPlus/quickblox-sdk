import 'dart:async';

import 'package:flutter/services.dart';
import 'package:quickblox_sdk/mappers/qb_custom_object_mapper.dart';
import 'package:quickblox_sdk/models/qb_custom_object.dart';
import 'package:quickblox_sdk/models/qb_filter.dart';
import 'package:quickblox_sdk/models/qb_sort.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class CustomObjects {
  const CustomObjects();

  ///////////////////////////////////////////////////////////////////////////
  // CUSTOM OBJECTS MODULE
  ///////////////////////////////////////////////////////////////////////////

  //Channel name
  static const CHANNEL_NAME = "FlutterQBCustomObjectsChannel";

  //Methods
  static const CREATE_METHOD = "create";
  static const REMOVE_METHOD = "remove";
  static const GET_BY_IDS_METHOD = "getByIds";
  static const GET_METHOD = "get";
  static const UPDATE_METHOD = "update";

  //Module
  static const _customObjectsModule = const MethodChannel(CHANNEL_NAME);

  Future<List<QBCustomObject>> create(
      {String className,
      Map<String, Object> fields,
      List<Map<String, Object>> objects}) async {
    Map<String, Object> data = new Map();

    if (className != null) {
      data["className"] = className;
    }
    if (fields != null) {
      data["fields"] = fields;
    }
    if (objects != null) {
      data["objects"] = objects;
    }
    List<Object> list =
        await _customObjectsModule.invokeMethod(CREATE_METHOD, data);

    List<QBCustomObject> customObjectList = new List();

    for (final item in list) {
      Map<String, Object> customObjectMap = Map.castFrom(item);
      QBCustomObject customObject =
          QBCustomObjectMapper.mapToQBCustomObject(customObjectMap);
      customObjectList.add(customObject);
    }

    return customObjectList;
  }

  Future<void> remove(String className, List<String> ids) async {
    Map<String, Object> data = new Map();
    if (className != null) {
      data["className"] = className;
    }
    if (ids != null) {
      data["ids"] = ids;
    }
    await _customObjectsModule.invokeMethod(REMOVE_METHOD, data);
  }

  Future<List<QBCustomObject>> getByIds(
      String className, List<String> ids) async {
    Map<String, Object> data = new Map();
    if (className != null) {
      data["className"] = className;
    }
    if (ids != null) {
      data["objectsIds"] = ids;
    }
    List<Object> list =
        await _customObjectsModule.invokeMethod(GET_BY_IDS_METHOD, data);

    List<QBCustomObject> customObjectList = new List();

    for (final item in list) {
      Map<String, Object> customObjectMap = Map.castFrom(item);
      QBCustomObject customObject =
          QBCustomObjectMapper.mapToQBCustomObject(customObjectMap);
      customObjectList.add(customObject);
    }

    return customObjectList;
  }

  Future<List<QBCustomObject>> get(String className,
      {QBSort sort,
      QBFilter filter,
      int limit = 100,
      int skip = 0,
      List<String> exclude,
      List<String> include}) async {
    Map<String, Object> data = new Map();
    if (className != null) {
      data["className"] = className;
    }
    if (sort != null) {
      data["sort"] = sort;
    }
    if (filter != null) {
      data["filter"] = filter;
    }
    if (limit != null) {
      data["limit"] = limit;
    }
    if (skip != null) {
      data["skip"] = skip;
    }
    if (include != null) {
      data["include"] = include;
    }
    if (exclude != null) {
      data["exclude"] = exclude;
    }
    List<Object> list =
        await _customObjectsModule.invokeMethod(GET_METHOD, data);

    List<QBCustomObject> customObjectList = new List();

    for (final item in list) {
      Map<String, Object> customObjectMap = Map.castFrom(item);
      QBCustomObject customObject =
          QBCustomObjectMapper.mapToQBCustomObject(customObjectMap);
      customObjectList.add(customObject);
    }

    return customObjectList;
  }

  Future<QBCustomObject> update(String className,
      {String id,
      Map<String, Object> fields,
      Map<String, Object> objects}) async {
    Map<String, Object> data = new Map();
    if (className != null) {
      data["className"] = className;
    }
    if (id != null) {
      data["id"] = id;
    }
    if (fields != null) {
      data["fields"] = fields;
    }
    if (objects != null) {
      data["objects"] = objects;
    }
    Object object = new Map<String, dynamic>.from(
        await _customObjectsModule.invokeMethod(UPDATE_METHOD, data));

    Map<String, Object> customObjectMap = Map.castFrom(object);
    QBCustomObject customObject =
        QBCustomObjectMapper.mapToQBCustomObject(customObjectMap);

    return customObject;
  }
}
