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

import org.json.JSONObject;

import java.io.IOException;

public class Command {
    private static BroadcastReceiver myReceiver;

    public static void execute(Context context, String command, JSONObject jsonObject) {
        Log.d("Command", "Executing command: " + command);

        final NovaUtils novaUtils = new NovaUtils(context);

        if (command.startsWith("open")) {
            String msg = novaUtils.openApp(jsonObject.optString("packageName"));
            if (!msg.equals("Ongoing")) {
                OverlayWindow.destroy();
                getSpeech(context, msg);
            }
        } else if (command.startsWith("call")) {

        } else if (command.startsWith("set")) {

        } else if (command.startsWith("play")) {

        } else if (command.startsWith("send")) {

        } else if (command.startsWith("email")) {

        } else if (command.startsWith("whatsapp")) {

        } else if (command.startsWith("telegram")) {

        } else if (command.startsWith("go home")) {

        } else if (command.startsWith("check")) {
            performCheckOperation(command);
        } else if (command.startsWith("on")) {

        } else if (command.startsWith("off")) {

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
