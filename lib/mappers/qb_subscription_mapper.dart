import 'package:quickblox_sdk/models/qb_subscription.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBSubscriptionMapper {
  static QBSubscription mapToQBSubscription(Map<String, Object> map) {
    if (map == null || map.length <= 0) {
      return null;
    }

    QBSubscription subscription = new QBSubscription();

    if (map.containsKey("id")) {
      subscription.id = map["id"];
    }
    if (map.containsKey("deviceUdid")) {
      subscription.deviceUdid = map["deviceUdid"];
    }
    if (map.containsKey("deviceToken")) {
      subscription.deviceToken = map["deviceToken"];
    }
    if (map.containsKey("devicePlatform")) {
      subscription.devicePlatform = map["devicePlatform"];
    }
    if (map.containsKey("pushChannel")) {
      subscription.pushChannel = map["pushChannel"];
    }

    return subscription;
  }
}
