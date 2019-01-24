import 'dart:async';

import 'package:flutter/services.dart';

class ImeiPlugin {
  static const MethodChannel _channel =
      const MethodChannel('imei_plugin');

  static Future<String> get getImei async {
    final String version = await _channel.invokeMethod('getImei');
    return version;
  }
}
