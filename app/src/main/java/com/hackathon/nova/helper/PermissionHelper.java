package com.hackathon.nova.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionHelper {

    public static void requestLocationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION
                }, requestCode);
            }
        } else { // Android 9 and below
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, requestCode);
            }
        }
    }

}
