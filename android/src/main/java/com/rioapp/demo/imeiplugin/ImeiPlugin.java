package com.rioapp.demo.imeiplugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * ImeiPlugin
 */
    public class ImeiPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {
    private MethodChannel channel;
    private Activity activity;
    private ActivityPluginBinding pluginBinding;


    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1995;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE_IMEI_MULTI = 1997;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID_99599";
    private Result mResult;
    private static boolean ssrpr = false;
    private static final String ERCODE_PERMISSIONS_DENIED = "2000";

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "imei_plugin");
        channel.setMethodCallHandler(this);
    }

    private static void getImei(Activity activity, Result result) {
        try {

            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                result.success(getUUID(activity));
            }else if (ContextCompat.checkSelfPermission((activity), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    result.success( telephonyManager.getImei() );
                else
                    result.success( telephonyManager.getDeviceId() );

            } else {

                if (ssrpr && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE) )
                    result.error(ERCODE_PERMISSIONS_DENIED,"Permission Denied", null);
                else
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            }

        } catch (Exception ex) {
            result.success("unknown");
        }
    }

    private static void getImeiMulti(Activity activity, Result result) {
        try {

            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                result.success(Arrays.asList(getUUID(activity)));
            }else if (ContextCompat.checkSelfPermission((activity), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int phoneCount = telephonyManager.getPhoneCount();

                    ArrayList<String> imeis = new ArrayList<>();
                    for (int i = 0; i < phoneCount; i++) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            imeis.add(telephonyManager.getImei(i));
                        else
                            imeis.add(telephonyManager.getDeviceId(i));
                    }
                    result.success(imeis);
                } else {
                    result.success( Arrays.asList(telephonyManager.getDeviceId()) );
                }

            } else {
                if (ssrpr && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE) )
                    result.error(ERCODE_PERMISSIONS_DENIED,"Permission Denied", null);
                else
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE_IMEI_MULTI);

            }

        } catch (Exception ex) {
            result.success("unknown");
        }
    }

    private synchronized static String getUUID(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_UNIQUE_ID, uniqueID);
            editor.commit();
        }

        return uniqueID;
    }

    private static void getID(Context context, Result result){
        result.success(getUUID(context));
    }

    @Override
    public void onMethodCall(@NotNull MethodCall call, @NonNull Result result) {
        mResult = result;

        try {
            ssrpr = call.<Boolean>argument("ssrpr");
        } catch (Exception e){
            ssrpr = false;
        }

        switch (call.method) {
            case "getImei":
                getImei(activity, mResult);
                break;
            case "getImeiMulti":
                getImeiMulti(activity, result);
                break;
            case "getId":
                getID(activity, result);
                break;
            default:
                mResult.notImplemented();
                break;
        }

    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE || requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE_IMEI_MULTI) {
            if (results[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
                    getImei(activity, mResult);
                } else {
                    getImeiMulti(activity, mResult);
                }
            } else {
                mResult.error(ERCODE_PERMISSIONS_DENIED, "Permission Denied", null);
            }
            return true;
        }

        return false;
    }

    private void attachToActivity(ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
        this.pluginBinding = binding;
        pluginBinding.addRequestPermissionsResultListener(this);
    }

    private void detachFromActivity() {
        this.activity = null;
        pluginBinding.removeRequestPermissionsResultListener(this);
        pluginBinding = null;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        attachToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        detachFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        attachToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        detachFromActivity();
    }
}
