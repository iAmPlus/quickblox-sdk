import 'dart:async';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */

typedef void RTCVideoViewCreatedCallback(RTCVideoViewController controller);

class RTCVideoView extends StatefulWidget {
  const RTCVideoView({
    Key key,
    this.onVideoViewCreated,
  }) : super(key: key);

  final RTCVideoViewCreatedCallback onVideoViewCreated;

  @override
  _RTCVideoViewState createState() => new _RTCVideoViewState();
}

class _RTCVideoViewState extends State<RTCVideoView> {
  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return new Transform(
          transform: Matrix4.identity()..rotateY(-pi),
          alignment: FractionalOffset.center,
          child: AndroidView(
            viewType: 'QBWebRTCViewFactory',
            onPlatformViewCreated: _onPlatformViewCreated,
          ));
    } else if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'QBWebRTCViewFactory',
        onPlatformViewCreated: _onPlatformViewCreated,
      );
    }

    return Text(
        '$defaultTargetPlatform is not yet supported by the QBWebRTC plugin');
  }

  void _onPlatformViewCreated(int id) {
    if (widget.onVideoViewCreated == null) {
      return;
    }
    widget.onVideoViewCreated(new RTCVideoViewController._(id));
  }
}

class RTCVideoViewController {
  //WebRTCVideoView Methods
  static const SET_MIRROR_METHOD = "mirror";
  static const SET_SCALE_TYPE_METHOD = "scaleType";
  static const PLAY_METHOD = "play";
  static const RELEASE_METHOD = "release";
  static const CALL_METHOD = "call";
  static const STOP_METHOD = "stop";

  RTCVideoViewController._(int id)
      : _channel = new MethodChannel('QBWebRTCFlutterVideoViewChannel/$id');

  final MethodChannel _channel;

  Future<void> setMirror(bool mirror) async {
    Map<String, Object> values = Map();
    if (mirror != null) {
      values["mirror"] = mirror;
    }
    return _channel.invokeMethod(SET_MIRROR_METHOD, values);
  }

  Future<void> setScaleType(int scalingType) async {
    Map<String, Object> values = Map();
    if (scalingType != null) {
      values["scalingType"] = scalingType;
    }
    return _channel.invokeMethod(SET_SCALE_TYPE_METHOD, values);
  }

  Future<void> play(String sessionId, int userId) async {
    Map<String, Object> values = Map();

    if (sessionId != null) {
      values["sessionId"] = sessionId;
    }

    if (userId != null) {
      values["userId"] = userId;
    }

    await _channel.invokeMethod(PLAY_METHOD, values);
  }

  Future<void> stop(String sessionId, int userId) async {
    Map<String, Object> values = Map();

    if (sessionId != null) {
      values["sessionId"] = sessionId;
    }

    if (userId != null) {
      values["userId"] = userId;
    }

    await _channel.invokeMethod(STOP_METHOD, values);
  }

  Future<void> release() async {
    await _channel.invokeMethod(RELEASE_METHOD);
  }
}
