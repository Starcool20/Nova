package com.hackathon.nova.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hackathon.nova.R;

public class ForegroundService extends Service {
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String type = intent.getStringExtra("TYPE");
        final Intent intent2 = intent.getParcelableExtra("INTENT");
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nova Is Running")
                .setContentText("Nova foreground service")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1, notification);

        // TODO: Your service logic here (e.g., playing music, tracking location)

        try {
            startActivity(intent2);
            sendBroadcast(true, "SUCCESS");
        } catch (Exception e) {
            Log.d("Nova", "onStartCommand: " + e.getMessage());
            sendBroadcast(false, e.getMessage());
        }

        stopMyService();

        return START_STICKY;
    }

    private void sendBroadcast(boolean data, String text) {
        Intent intent = new Intent();
        intent.setAction("com.hackathon.nova.COMMAND_FOREGROUND_SERVICE");
        intent.putExtra("ISSUCCESS", data);
        intent.putExtra("MESSAGE", text);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopMyService() {
        stopForeground(true); // Remove the notification
        stopSelf(); // Stop the service
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}

