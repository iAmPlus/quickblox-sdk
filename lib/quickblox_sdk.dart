import 'auth/module.dart';
import 'chat/module.dart';
import 'customobjects/module.dart';
import 'file/module.dart';
import 'notifications/module.dart';
import 'push/module.dart';
import 'settings/module.dart';
import 'users/module.dart';
import 'webrtc/module.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QB {
  static const auth = const Auth();
  static const chat = const Chat();
  static const data = const CustomObjects();
  static const content = const File();
  static const events = const Notifications();
  static const subscriptions = const Push();
  static const settings = const Settings();
  static const users = const Users();
  static const webrtc = const WebRTC();
}
