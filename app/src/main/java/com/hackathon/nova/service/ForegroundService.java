package com.hackathon.nova.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hackathon.nova.R;
import com.hackathon.nova.command.Command;

public class ForegroundService extends Service {
    public static final String EXTRA_DATA = "Nova";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String value = intent.getStringExtra(EXTRA_DATA);
        final String type = intent.getStringExtra("TYPE");
        final int hour = intent.getIntExtra("HOUR", 0);
        final int minutes = intent.getIntExtra("MINUTES", 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nova Is Running")
                .setContentText("Nova foreground service")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1, notification);

        // TODO: Your service logic here (e.g., playing music, tracking location)

        assert type != null;
        if (type.equals("open_app")) {
            assert value != null;
            final Intent launchIntent = getPackageManager().getLaunchIntentForPackage(value);
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
            }
        } else if (type.equals("call_contact")) {
            final Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + value));
            startActivity(intent2);
        } else if (type.equals("set_alarm")) {
            final Intent intent2 = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, value);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);

            try {
                startActivity(intent2);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to set alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (type.equals("play_song")) {
            Intent intent2 = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
            intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.TITLE);
            intent.putExtra(SearchManager.QUERY, value);

            try {
                startActivity(Intent.createChooser(intent2, "Select a Music Player"));
            } catch (Exception e) {
                Toast.makeText(this, "Failed to play song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        stopMyService();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMyService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopMyService() {
        stopForeground(true); // Remove the notification
        stopSelf(); // Stop the service

        Command.unRegisterReceiver(this);
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

