#import "ImeiPlugin.h"

@implementation ImeiPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"imei_plugin"
            binaryMessenger:[registrar messenger]];
  ImeiPlugin* instance = [[ImeiPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getImei" isEqualToString:call.method]) {
    NSUUID *identifierForVendor = [[UIDevice currentDevice] identifierForVendor];
    result([identifierForVendor UUIDString]);
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
