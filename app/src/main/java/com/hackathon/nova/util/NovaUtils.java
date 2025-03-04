package com.hackathon.nova.util;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.hackathon.nova.command.Command;
import com.hackathon.nova.service.ForegroundService;

import java.io.File;

public class NovaUtils {
    private static final int REQUEST_PERMISSION = 1;
    private final Context context;

    public NovaUtils(Context context) {
        this.context = context;
    }

    public void startService(String packageName, int hour, int minutes, String type) {
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        serviceIntent.putExtra(ForegroundService.EXTRA_DATA, packageName);
        serviceIntent.putExtra("TYPE", type);
        serviceIntent.putExtra("HOUR", hour);
        serviceIntent.putExtra("MINUTES", minutes);

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
    public String openApp(String packageName) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            return "App not found";
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // Android 11+
            startService(packageName, 0, 0, "open_app");
            return "Ongoing";
        }

        Command.registerReceiverService(context);

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);

        return "Success";
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
            startService(phoneNumber, 0, 0, "call_contact");
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(message, hour, minute, "set_alarm");
        } else {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Failed to set alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    // 🔹 Play Song
    public boolean playSong(String songName) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.TITLE);
        intent.putExtra(SearchManager.QUERY, songName);

        // Check if any app can handle the intent
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No app found to play the song", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(songName, 0, 0, "play_song");
        } else {
            try {
                context.startActivity(Intent.createChooser(intent, "Select a Music Player"));
            } catch (Exception e) {
                Toast.makeText(context, "Failed to play song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    public boolean sendSMS(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No SMS app found!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(message, 0, 0, "send_sms");
        } else {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    public String checkBattery() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (batteryManager != null) {
            int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            return "Battery Level: " + batteryLevel + "%";
        } else {
            return "Battery info not available";
        }
    }


    public String checkStorage() {
        File path = Environment.getDataDirectory(); // Internal storage
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long freeSpace = (availableBlocks * blockSize) / (1024 * 1024 * 1024); // Convert to GB

        return "Free Storage: " + freeSpace + " GB";
    }


    // 🔹 Check RAM
    public String checkRAM() {
        return "RAM information is not directly accessible in newer Android versions.";
    }

    // 🔹 Check Location
    public String checkLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    // 🔹 Check Internet (Works on API 21 to 36)
    public String checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return "No internet connection";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // ✅ Android 6+ (API 23+): Use getNetworkCapabilities()
            Network network = cm.getActiveNetwork();
            if (network == null) return "No internet connection";

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) return "No internet connection";

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return "Internet is available";
            }
        } else {
            // ✅ Android 5 (API 21-22): Use the old method
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                return "Internet is available";
            }
        }

        return "No internet connection";
    }

    public void sendMessageToWhatsApp(String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.whatsapp");
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendBroadcast(String action, boolean data) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("isSuccess", data);
        context.sendBroadcast(intent);
    }
}