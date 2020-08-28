import 'dart:async';

import 'package:flutter/services.dart';
import 'package:quickblox_sdk/mappers/qb_file_mapper.dart';
import 'package:quickblox_sdk/models/qb_file.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class File {
  const File();

  ///////////////////////////////////////////////////////////////////////////
  // FILE MODULE
  ///////////////////////////////////////////////////////////////////////////

  //Channel name
  static const CHANNEL_NAME = "FlutterQBFileChannel";

  //Methods
  static const SUBSCRIBE_UPLOAD_PROGRESS_METHOD = "subscribeUploadProgress";
  static const UNSUBSCRIBE_UPLOAD_PROGRESS_METHOD = "unsubscribeUploadProgress";
  static const UPLOAD_METHOD = "upload";
  static const GET_INFO_METHOD = "getInfo";
  static const GET_PUBLIC_URL_METHOD = "getPublicURL";
  static const GET_PRIVATE_URL_METHOD = "getPrivateURL";

  //Module
  static const _fileModule = const MethodChannel(CHANNEL_NAME);

  Future<StreamSubscription<dynamic>> subscribeUploadProgress(
      String url, String eventName, eventMethod,
      {onErrorMethod}) async {
    Map<String, Object> data = new Map();
    if (url != null) {
      data["url"] = url;
    }

    await _fileModule.invokeMethod(SUBSCRIBE_UPLOAD_PROGRESS_METHOD, data);
    return EventChannel(eventName)
        .receiveBroadcastStream(eventName)
        .listen(eventMethod, onError: onErrorMethod);
  }

  Future<void> unsubscribeUploadProgress(String url, String eventName) async {
    Map<String, Object> data = new Map();
    if (url != null) {
      data["url"] = url;
    }

    await _fileModule.invokeMethod(UNSUBSCRIBE_UPLOAD_PROGRESS_METHOD, data);
  }

  Future<QBFile> upload(String url, {bool public = false}) async {
    Map<String, Object> data = new Map();
    if (url != null) {
      data["url"] = url;
    }
    if (public != null) {
      data["public"] = public;
    }
    Object object = await _fileModule.invokeMethod(UPLOAD_METHOD, data);
    Map<String, Object> fileMap = Map.castFrom(object);
    QBFile file = QBFileMapper.mapToQBFile(fileMap);
    return file;
  }

  Future<QBFile> getInfo(int id) async {
    Map<String, Object> data = new Map();

    if (id != null) {
      data["id"] = id;
    }

    Object object = await _fileModule.invokeMethod(GET_INFO_METHOD, data);

    Map<String, Object> fileMap = Map.castFrom(object);
    QBFile file = QBFileMapper.mapToQBFile(fileMap);
    return file;
  }

  Future<String> getPublicURL(String uid) async {
    Map<String, Object> data = new Map();
    if (uid != null) {
      data["uid"] = uid;
    }

    return await _fileModule.invokeMethod(GET_PUBLIC_URL_METHOD, data);
  }

  Future<String> getPrivateURL(String uid) async {
    Map<String, Object> data = new Map();
    if (uid != null) {
      data["uid"] = uid;
    }

    return await _fileModule.invokeMethod(GET_PRIVATE_URL_METHOD, data);
  }
}
