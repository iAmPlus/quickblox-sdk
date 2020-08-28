import 'package:quickblox_sdk/models/qb_sort.dart';

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class QBSortMapper {
  static Map<String, Object> sortToMap(QBSort qbSort) {
    if (qbSort == null) {
      return null;
    }

    Map<String, Object> map = new Map();

    if (qbSort.field != null) {
      map["field"] = qbSort.field;
    }
    if (qbSort.ascending != null) {
      map["ascending"] = qbSort.ascending;
    }

    return map;
  }
}
