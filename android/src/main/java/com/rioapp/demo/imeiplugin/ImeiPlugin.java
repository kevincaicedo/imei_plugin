package com.rioapp.demo.imeiplugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * ImeiPlugin
 */
public class ImeiPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
    private final Activity activity;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1995;
    private Result mResult;

    /**
     * Plugin registration.
     * add Listener Request permission
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "imei_plugin");
        ImeiPlugin imeiPlugin = new ImeiPlugin(registrar.activity());
        channel.setMethodCallHandler(imeiPlugin);
        registrar.addRequestPermissionsResultListener(imeiPlugin);
    }

    private ImeiPlugin(Activity activity) {
        this.activity = activity;
    }

    public static void getImei(Context context, Result result) {
        try {

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            if (ContextCompat.checkSelfPermission((context), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    result.success( telephonyManager.getImei() );
                else
                    result.success( telephonyManager.getDeviceId() );

            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_PHONE_STATE) )
                    result.success("Permission Denied");
                else
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            }

        } catch (Exception ex) {
            result.success("unknown");
        }
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        mResult = result;

        if (call.method.equals("getImei"))
            getImei((Context) activity, mResult);
        else
            mResult.notImplemented();

    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] ints) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if (ints.length > 0 && ints[0] == PackageManager.PERMISSION_GRANTED) {
                    getImei(activity, mResult);
                } else {
                    mResult.success("Permission Denied");
                    // permission denied
                }
                break;
        }
        return true;
    }

}
