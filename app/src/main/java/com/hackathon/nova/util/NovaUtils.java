package com.hackathon.nova.util;

import android.Manifest;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
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
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.hackathon.nova.command.Command;
import com.hackathon.nova.helper.PermissionHelper;
import com.hackathon.nova.service.ForegroundService;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NovaUtils {
    private static final int REQUEST_PERMISSION = 1;
    private final Context context;

    public NovaUtils(Context context) {
        this.context = context;
    }

    public void startService(Intent intent, String type) {
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        serviceIntent.putExtra("INTENT", intent);
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
    public String openApp(String packageName) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            return "App not found";
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // Android 11+
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Command.registerReceiverService(context);
            startService(launchIntent, "open_app");
            return "Pending";
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);

        return "Success";
    }

    public String callContact(String phoneNumber) {
        if (!PermissionHelper.isCallPermissionGranted(context)) {
            Toast.makeText(context, "Call permission needed", Toast.LENGTH_SHORT).show();
            return "Call permission needed";
        }

        final Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (intent2.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No app found to make a call", Toast.LENGTH_SHORT).show();
            return "No app found to make a call";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Command.registerReceiverService(context);
            startService(intent2, "call_contact");
            return "Pending";
        }
            context.startActivity(intent2);
        return "Success";
    }


    public String setAlarm(String message, int hour, int minute) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true); // Skip UI if possible

        // Check if there's an app that can handle the intent
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No app found to set an alarm", Toast.LENGTH_SHORT).show();
            return "No app found to set an alarm";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Command.registerReceiverService(context);
            startService(intent, "set_alarm");
            return "Pending";
        }

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Failed to set alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return "Failed to set alarm";
            }
        return "Success";
    }


    // 🔹 Play Song
    public String playSong(String songName) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.TITLE);
        intent.putExtra(SearchManager.QUERY, songName);

        // Check if any app can handle the intent
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No app found to play the song", Toast.LENGTH_SHORT).show();
            return "No app found to play the song";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Command.registerReceiverService(context);
            startService(intent, "play_song");
            return "Pending";
        }
            try {
                context.startActivity(Intent.createChooser(intent, "Select a Music Player"));
            } catch (Exception e) {
                Toast.makeText(context, "Failed to play song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return "Failed to play song";
            }
        return "Success";
    }


    public String sendSMS(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            Toast.makeText(context, "No SMS app found!", Toast.LENGTH_SHORT).show();
            return "No SMS app found!";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Command.registerReceiverService(context);
            startService(intent, "send_sms");
            return "Pending";
        }

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return "Failed to send SMS";
            }
        return "Success";
    }

    public String sendEmail(Context context, String toEmail, String subject, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("com.google.android.gm"); // Opens Gmail specifically

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Command.registerReceiverService(context);
            startService(intent, "send_sms");
            return "Pending";
        }

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Gmail app not installed!", Toast.LENGTH_SHORT).show();
            return "Gmail app not installed!";
        }
        return "Success";
    }



    public String checkBattery() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (batteryManager != null) {
            int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            return "Battery Level Is: " + batteryLevel + "%";
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


    public String getRAMInfo() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            long totalRam = memoryInfo.totalMem / (1024 * 1024); // Convert to MB
            long availableRam = memoryInfo.availMem / (1024 * 1024); // Convert to MB

            return "Total RAM: " + totalRam + " MB\nAvailable RAM: " + availableRam + " MB";
        }
        return "RAM info not available";
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

    public String checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return "No internet connection";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // ✅ Android 6+ (API 23+): Use NetworkCapabilities
            Network network = cm.getActiveNetwork();
            if (network == null) return "No internet connection";

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) return "No internet connection";

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return "Connected to Wi-Fi";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "Connected to Mobile Data";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return "Connected to Ethernet";
            }
        } else {
            // ✅ Android 5 (API 21-22): Use NetworkInfo (deprecated in API 29)
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "Connected to Wi-Fi";
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return "Connected to Mobile Data";
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    return "Connected to Ethernet";
                }
            }
        }

        return "No internet connection";
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            return false;
        }
    }


    public String sendMessageToWhatsApp(String phoneNumber, String message) {
        PackageManager packageManager = context.getPackageManager();
        try {
            // Check if WhatsApp is installed
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
            intent.setData(Uri.parse(url));

            if (isAppInstalled("com.whatsapp", packageManager)) {
                intent.setPackage("com.whatsapp"); // Use WhatsApp
            } else if (isAppInstalled("com.whatsapp.w4b", packageManager)) {
                intent.setPackage("com.whatsapp.w4b"); // Use WhatsApp Business
            } else {
                return "WhatsApp is not installed!";
            }

            context.startActivity(intent);
            return "Success";
        } catch (Exception e) {
            Toast.makeText(context, "Failed to open WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return "Failed";
        }
    }


    public String sendMessageToTelegram(String phoneNumber, String message) {
        PackageManager packageManager = context.getPackageManager();
        try {
            // Convert phone number to international format (ensure it starts with +)
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+" + phoneNumber;
            }

            // Encode the message properly
            String url = "https://t.me/" + phoneNumber + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            // Check if Telegram or Telegram X is installed
            if (isAppInstalled("org.telegram.messenger", packageManager)) {
                intent.setPackage("org.telegram.messenger"); // Telegram app
            } else if (isAppInstalled("org.thunderdog.challegram", packageManager)) {
                intent.setPackage("org.thunderdog.challegram"); // Telegram X
            } else {
                return "Telegram is not installed!";
            }

            context.startActivity(intent);
            return "Success";
        } catch (Exception e) {
            return "Failed";
        }
    }

    // Helper method to check if an app is installed
    private boolean isAppInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public String isVibrationWorking(Context context) {
        Vibrator vibrator;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+): Use VibratorManager
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            // Older versions (API 21-30)
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator == null || !vibrator.hasVibrator()) {
            return "Vibration NOT supported on this device.";
        }

        return "Vibration is available!";
    }

    public String getPrimaryLanguage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else {
            return context.getResources().getConfiguration().locale.getLanguage();
        }
    }

    public String setBrightness(AppCompatActivity activity, float brightness) {
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = brightness; // Value between 0.0 and 1.0
            window.setAttributes(layoutParams);
        } catch (Exception e) {
            return "Failed";
        }
        return "Success";
    }
}