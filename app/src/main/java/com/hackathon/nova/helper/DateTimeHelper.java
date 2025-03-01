package com.hackathon.nova.helper;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {
    public static String getCurrentDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26+ (Android 8.0 and above)
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()));
        } else {
            // For API 21-25
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dateFormat.format(new Date());
        }
    }

    public static String getCurrentTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26+ (Android 8.0 and above)
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()));
        } else {
            // For API 21-25
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            return timeFormat.format(new Date());
        }
    }
}

