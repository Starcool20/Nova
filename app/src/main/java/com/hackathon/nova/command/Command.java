package com.hackathon.nova.command;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.hackathon.nova.overlay.OverlayWindow;
import com.hackathon.nova.util.NovaUtils;

import java.io.IOException;

public class Command {
    private static BroadcastReceiver myReceiver;

    public static void execute(Context context, String command) {
        final String commands = command.toLowerCase();
        final NovaUtils novaUtils = new NovaUtils(context);

        if (command.startsWith("open")) {
            String msg = novaUtils.openApp(command.substring(5).trim());
            if (!msg.equals("Ongoing")) {
                getSpeech(context, msg);
            }
        } else if (commands.startsWith("call")) {

        } else if (commands.startsWith("set alarm")) {

        } else if (commands.startsWith("play")) {

        } else if (commands.startsWith("send message")) {

        } else if (commands.startsWith("add event")) {

        } else if (commands.startsWith("go home")) {

        } else if (commands.startsWith("check")) {
            performCheckOperation(command);
        } else if (commands.startsWith("on")) {

        } else if (commands.startsWith("off")) {

        }
    }

    private static void performCheckOperation(String command) {
        String words = command.substring(5).toLowerCase();

        switch (words) {
            case "battery percentage":

                break;
            case "storage":

                break;
            case "ram":

                break;
            case "location":

                break;
            case "wifi":

                break;
            case "internet":

                break;
            case "speaker":

                break;
            case "microphone":

                break;
            case "vibration":

                break;
            case "language":

                break;
            case "brightness":

                break;
            case "volume":

                break;
            case "weather":

                break;
            case "news":

                break;
            case "contact list":

                break;
            case "message history":

                break;
            case "notification history":

                break;
            case "bluetooth":

                break;
            default:

                break;
        }
    }

    public static void registerReceiverService(Context context) {
        // Define the receiver inside the activity
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String text = intent.getStringExtra("SUCCESS");
                getSpeech(context, text);
            }
        };

        // Register the receiver
        IntentFilter filter = new IntentFilter("com.hackathon.nova.COMMAND_FOREGROUND_SERVICE");
        ContextCompat.registerReceiver(context, myReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    public static void unRegisterReceiver(Context context) {
        if (myReceiver != null) {
            context.unregisterReceiver(myReceiver);
            myReceiver = null;
        }
    }

    private static void getSpeech(Context context, String text) {
        if (text != null) {
            if (text.equals("App not found")) {
                playAudio(context, "nova_app_not_found");
            } else if (text.equals("Success")) {
                playAudio(context, "nova_done");
            } else if (text.equals("Ongoing")) {
                playAudio(context, "error");
            }
        }
    }

    private static void playAudio(Context context, String audioFile) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context.getExternalFilesDir(null) + "/" + audioFile + ".mp3");
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                OverlayWindow.destroy();
            });
        } catch (IOException e) {
            Log.e("Command", "Error playing audio: " + e.getMessage());
        }
    }
}
