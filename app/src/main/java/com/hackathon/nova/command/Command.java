package com.hackathon.nova.command;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

import com.hackathon.nova.util.NovaUtils;

public class Command {
    private static BroadcastReceiver myReceiver;

    public static void execute(Context context, String command) {
        final String commands = command.toLowerCase();
        final NovaUtils novaUtils = new NovaUtils(context);

        if (command.startsWith("open")) {
            String msg = novaUtils.openApp(command.substring(5).trim());
            if (!msg.equals("Ongoing")) {
                getSpeech(context);
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

            }
        };

        // Register the receiver
        IntentFilter filter = new IntentFilter("com.hackathon.nova.COMMAND_FORGROUND_SERVICE");
        ContextCompat.registerReceiver(context, myReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    public static void unRegisterReceiver(Context context) {
        if (myReceiver != null) {
            context.unregisterReceiver(myReceiver);
            myReceiver = null;
        }
    }

    private static void getSpeech(Context context) {

    }
}
