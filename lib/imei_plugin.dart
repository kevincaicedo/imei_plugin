import 'dart:async';

import 'package:flutter/services.dart';

class ImeiPlugin {
  static const MethodChannel _channel = const MethodChannel('imei_plugin');

  // get imei android device @return String
  static Future<String> getImei({
    bool shouldShowRequestPermissionRationale = false
  }) async {
    final String imei = await _channel.invokeMethod('getImei', { "ssrpr": shouldShowRequestPermissionRationale });
    return imei;
  }
}
