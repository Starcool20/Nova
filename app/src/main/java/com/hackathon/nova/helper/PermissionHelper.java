package com.hackathon.nova.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PermissionHelper {

    public static boolean isLocationPermissionGranted(Context context) {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestLocationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION
                }, requestCode);
        } else { // Android 9 and below
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, requestCode);
        }
    }

    public static boolean hasContactListPermission(Activity context) {
        // Check & request permissions
        return context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestContactListPermission(Activity context, int REQUEST_CONTACTS_PERMISSION) {
        // Check & request permissions
        context.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS_PERMISSION);
    }

    public static boolean isRecordAudioPermissionGranted(Context context) {
        return context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestRecordAudioPermission(Activity context, int REQUEST_RECORD_AUDIO_PERMISSION) {
        context.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    public static boolean isAccessibilityServiceEnabled(
            Context context, Class<?> accessibilityServiceClass) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + accessibilityServiceClass.getName();

        try {
            accessibilityEnabled =
                    Settings.Secure.getInt(
                            context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("AccessibilityService", "Error finding setting: " + e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String enabledServices =
                    Settings.Secure.getString(
                            context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (enabledServices != null) {
                TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
                splitter.setString(enabledServices);
                while (splitter.hasNext()) {
                    String serviceName = splitter.next();
                    if (serviceName.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static void requestAccessibilityPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }

    public static boolean isCallPermissionGranted(Context context) {
        return context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCallPermission(Activity context, int REQUEST_CALL_PERMISSION) {
        context.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
    }

}
