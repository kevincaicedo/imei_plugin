package com.rioapp.demo.imeiplugin.imei_plugin;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
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
public class ImeiPlugin implements FlutterPlugin, PluginRegistry.RequestPermissionsResultListener, MethodCallHandler, ActivityAware {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1995;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE_IMEI_MULTI = 1997;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID_99599";
    private static final String ERCODE_PERMISSIONS_DENIED = "2000";
    private static boolean ssrpr = false;
    private Activity activity;
    private Result mResult;
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    private static void getImei(Activity activity, Result result) {
        try {

            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                result.success(getUUID(activity));
            } else if (ContextCompat.checkSelfPermission((activity), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    result.success(telephonyManager.getImei());
                else
                    result.success(telephonyManager.getDeviceId());

            } else {

                if (ssrpr && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE))
                    result.error(ERCODE_PERMISSIONS_DENIED, "Permission Denied", null);
                else
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            }

        } catch (Exception ex) {
            result.success("unknown");
        }
    }

    private static void getImeiMulti(Activity activity, Result result) {
        try {

            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                result.success(Arrays.asList(getUUID(activity)));
            } else if (ContextCompat.checkSelfPermission((activity), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
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
                    result.success(Arrays.asList(telephonyManager.getDeviceId()));
                }

            } else {
                if (ssrpr && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE))
                    result.error(ERCODE_PERMISSIONS_DENIED, "Permission Denied", null);
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

    private static void getID(Context context, Result result) {
        result.success(getUUID(context));
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "imei_plugin");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        mResult = result;

        try {
            ssrpr = call.<Boolean>argument("ssrpr");
        } catch (Exception e) {
            ssrpr = false;
        }

        if (call.method.equals("getImei"))
            getImei(activity, mResult);
        else if (call.method.equals("getImeiMulti"))
            getImeiMulti(activity, result);
        else if (call.method.equals("getId"))
            getID(activity, result);
        else
            mResult.notImplemented();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
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

    /**
     * This {@code ActivityAware} {@link FlutterPlugin} is now
     * associated with an {@link Activity}.
     *
     * <p>This method can be invoked in 1 of 2 situations:
     *
     * <ul>
     *   <li>This {@code ActivityAware} {@link FlutterPlugin} was
     *       just added to a {@link FlutterEngine} that was already
     *       connected to a running {@link Activity}.
     *   <li>This {@code ActivityAware} {@link FlutterPlugin} was
     *       already added to a {@link FlutterEngine} and that {@link
     *       FlutterEngine} was just connected to an {@link
     *       Activity}.
     * </ul>
     * <p>
     * The given {@link ActivityPluginBinding} contains {@link Activity}-related
     * references that an {@code ActivityAware} {@link
     * FlutterPlugin} may require, such as a reference to the
     * actual {@link Activity} in question. The {@link ActivityPluginBinding} may be
     * referenced until either {@link #onDetachedFromActivityForConfigChanges()} or {@link
     * #onDetachedFromActivity()} is invoked. At the conclusion of either of those methods, the
     * binding is no longer valid. Clear any references to the binding or its resources, and do not
     * invoke any further methods on the binding or its resources.
     *
     * @param binding
     */
    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
        binding.addRequestPermissionsResultListener(this);

    }

    /**
     * The {@link Activity} that was attached and made available in {@link
     * #onAttachedToActivity(ActivityPluginBinding)} has been detached from this {@code
     * ActivityAware}'s {@link FlutterEngine} for the purpose of
     * processing a configuration change.
     *
     * <p>By the end of this method, the {@link Activity} that was made available in
     * {@link #onAttachedToActivity(ActivityPluginBinding)} is no longer valid. Any references to the
     * associated {@link Activity} or {@link ActivityPluginBinding} should be cleared.
     *
     * <p>This method should be quickly followed by {@link
     * #onReattachedToActivityForConfigChanges(ActivityPluginBinding)}, which signifies that a new
     * {@link Activity} has been created with the new configuration options. That method
     * provides a new {@link ActivityPluginBinding}, which references the newly created and associated
     * {@link Activity}.
     *
     * <p>Any {@code Lifecycle} listeners that were registered in {@link
     * #onAttachedToActivity(ActivityPluginBinding)} should be deregistered here to avoid a possible
     * memory leak and other side effects.
     */
    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.activity = null;

    }

    /**
     * This plugin and its {@link FlutterEngine} have been re-attached to
     * an {@link Activity} after the {@link Activity} was recreated to handle
     * configuration changes.
     *
     * <p>{@code binding} includes a reference to the new instance of the {@link
     * Activity}. {@code binding} and its references may be cached and used from now until
     * either {@link #onDetachedFromActivityForConfigChanges()} or {@link #onDetachedFromActivity()}
     * is invoked. At the conclusion of either of those methods, the binding is no longer valid. Clear
     * any references to the binding or its resources, and do not invoke any further methods on the
     * binding or its resources.
     *
     * @param binding
     */
    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
        binding.addRequestPermissionsResultListener(this);

    }

    /**
     * This plugin has been detached from an {@link Activity}.
     *
     * <p>Detachment can occur for a number of reasons.
     *
     * <ul>
     *   <li>The app is no longer visible and the {@link Activity} instance has been
     *       destroyed.
     *   <li>The {@link FlutterEngine} that this plugin is connected to
     *       has been detached from its {@link FlutterView}.
     *   <li>This {@code ActivityAware} plugin has been removed from its {@link
     *       FlutterEngine}.
     * </ul>
     * <p>
     * By the end of this method, the {@link Activity} that was made available in {@link
     * #onAttachedToActivity(ActivityPluginBinding)} is no longer valid. Any references to the
     * associated {@link Activity} or {@link ActivityPluginBinding} should be cleared.
     *
     * <p>Any {@code Lifecycle} listeners that were registered in {@link
     * #onAttachedToActivity(ActivityPluginBinding)} or {@link
     * #onReattachedToActivityForConfigChanges(ActivityPluginBinding)} should be deregistered here to
     * avoid a possible memory leak and other side effects.
     */
    @Override
    public void onDetachedFromActivity() {
        this.activity = null;

    }
}
