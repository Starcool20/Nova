package com.hackathon.nova.util;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hackathon.nova.service.ForegroundService;

import java.io.File;

public class NovaUtils {
    private static final int REQUEST_PERMISSION = 1;
    private final Context context;

    public NovaUtils(Context context) {
        this.context = context;
    }

    public void startService(String packageName, String type) {
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        serviceIntent.putExtra(ForegroundService.EXTRA_DATA, packageName);
        serviceIntent.putExtra("TYPE", type);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent); // API 26+
        } else {
            context.startService(serviceIntent); // API 21-25
        }
    }

    public void stopService() {
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.stopService(serviceIntent);
    }

    // 🔹 Open App
    public void openApp(String packageName) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // Android 11+
            startService(packageName, "open_app");
        } else {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            } else {
                Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean callContact(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Call permission needed", Toast.LENGTH_SHORT).show();
            return false;
        }

        final Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (intent2.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No app found to make a call", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(phoneNumber, "call_contact");
        } else {
            context.startActivity(intent2);
        }
        return true;
    }


    public boolean setAlarm(String message, int hour, int minute) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);

        // Check if there's an app that can handle the intent
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No app found to set an alarm", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to set alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    // 🔹 Play Song
    public void playSong(String songName) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.TITLE);
        intent.putExtra(SearchManager.QUERY, songName);
        context.startActivity(intent);
    }

    // 🔹 Send SMS
    public void sendSMS(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermission((Activity) context, Manifest.permission.SEND_SMS);
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(context, "Message sent", Toast.LENGTH_SHORT).show();
    }

    // 🔹 Check Battery
    public String checkBattery() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return "Battery Level: " + batteryLevel + "%";
    }

    // 🔹 Check Storage
    public String checkStorage() {
        File path = Environment.getExternalStorageDirectory();
        long freeSpace = path.getFreeSpace() / (1024 * 1024 * 1024); // GB
        return "Free Storage: " + freeSpace + " GB";
    }

    // 🔹 Check RAM
    public String checkRAM() {
        return "RAM information is not directly accessible in newer Android versions.";
    }

    // 🔹 Check Temperature
    public String checkTemperature() {
        return "Temperature access requires root privileges.";
    }

    // 🔹 Check Location
    public String checkLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION);
            return "Location permission needed.";
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            return "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();
        } else {
            return "Location not available.";
        }
    }

    // 🔹 Check Internet
    public String checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected() ? "Internet is available" : "No internet connection";
    }

    // 🔹 Toggle Flashlight (API 23+)
    public void toggleFlashlight(boolean enable) {
        try {
            android.hardware.camera2.CameraManager cameraManager = (android.hardware.camera2.CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Request Permission (Runtime)
    private void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, REQUEST_PERMISSION);
    }
}

