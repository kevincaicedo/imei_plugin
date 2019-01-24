package com.rioapp.demo.imeiplugin;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** ImeiPlugin */
public class ImeiPlugin implements MethodCallHandler {
  private final Activity activity;
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "imei_plugin");
    channel.setMethodCallHandler(new ImeiPlugin(registrar.activity()));
  }

  private ImeiPlugin(Activity activity){
    this.activity = activity;
  }

  public static String getImei(Context context) {
    try {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    } catch (Exception ex) {
      return "";
    }
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getImei")) {
      result.success( getImei( (Context) activity ));
    } else {
      result.notImplemented();
    }
  }
}
