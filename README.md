# Imei Plugin

[ ![Build version](https://img.shields.io/badge/pub-v1.1.4-green)](https://pub.dev/packages/imei_plugin)

Get unique id device on ios and android

## Getting Started

Get IMEI (International Mobile Equipment Identity) for android devices with validate permission on ejecution time and
get unique id on ios An alphanumeric string that uniquely identifies a device to the app’s vendor.

**Use**
```dart
import 'package:imei_plugin/imei_plugin.dart';

var imei = await ImeiPlugin.getImei();
```

if you want to always request permission even if the user has already denied it. You can disable validation **shouldShowRequestPermissionRationale** set value in ```false```

```dart
String platformImei = await ImeiPlugin
    .getImei( shouldShowRequestPermissionRationale: false );
```
default value is ```false```.

### New Features!

- New param **shouldShowRequestPermissionRationale** only Android

### Platform Support

OS |
-- |
Android |
IOS |


License
----

MIT

### Author

This plugin is developed, **Free Software, by Kevin Caicedo**