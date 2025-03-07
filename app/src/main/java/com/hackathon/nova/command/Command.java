package com.hackathon.nova.command;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.hackathon.nova.helper.ContactListHelper;
import com.hackathon.nova.overlay.OverlayWindow;
import com.hackathon.nova.service.NovaAccessibilityService;
import com.hackathon.nova.util.NovaUtils;
import com.hackathon.nova.volume.VolumeControl;

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
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }
            String msg2 = novaUtils.callContact(phoneNumber);
            getSpeech(context, msg2);
        } else if (command.startsWith("set")) {
            String msg2 = novaUtils.setAlarm("NOVA ALARM", jsonObject.optInt("hour"), jsonObject.optInt("minutes"));
            getSpeech(context, msg2);
        } else if (command.startsWith("play")) {
            String msg3 = novaUtils.playSong(jsonObject.optString("songName"));
            getSpeech(context, msg3);
        } else if (command.startsWith("send")) {
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }

            String msg4 = novaUtils.sendSMS(phoneNumber, jsonObject.optString("message"));
            getSpeech(context, msg4);
        } else if (command.startsWith("email")) {
            String msg5 = novaUtils.sendEmail(context, jsonObject.optString("gmail"), "", jsonObject.optString("message"));
        } else if (command.startsWith("whatsapp")) {
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }

            String msg6 = novaUtils.sendMessageToWhatsApp(phoneNumber, jsonObject.optString("message"));
            getSpeech(context, msg6);
        } else if (command.startsWith("telegram")) {
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }

            String msg7 = novaUtils.sendMessageToTelegram(phoneNumber, jsonObject.optString("message"));
            getSpeech(context, msg7);
        } else if (command.contentEquals("go")) {
            new NovaAccessibilityService().goHome();
            Log.d("Command", "Going Home");
        } else if (command.contentEquals("check")) {
            performCheckOperation(context, command, novaUtils, jsonObject);
        } else {
            Log.d("Command", "Unknown command: " + command);
        }

        OverlayWindow.destroy();
    }

    private static void performCheckOperation(Context context, String command, NovaUtils novaUtils, JSONObject js) {
        switch (command) {
            case "battery percentage":
                String batteryInfo = novaUtils.checkBattery();
                getSpeech(context, batteryInfo);
                break;
            case "storage":
                String storageInfo = novaUtils.checkStorage();
                getSpeech(context, storageInfo);
                break;
            case "ram":
                String ramInfo = novaUtils.getRAMInfo();
                getSpeech(context, ramInfo);
                break;
            case "location":
                String locationInfo = novaUtils.checkLocation();
                getSpeech(context, locationInfo);
                break;
            case "internet":
                String internetInfo = novaUtils.checkInternet();
                if (novaUtils.isInternetAvailable()) {
                    internetInfo = "Connected to Internet";
                } else {
                    internetInfo = "No internet connection";
                }
                getSpeech(context, internetInfo);
                break;
            case "speaker":
                String speakerInfo = "If you can hear me then speaker is working correctly";
                getSpeech(context, speakerInfo);
                break;
            case "microphone":
                String microphoneInfo = "Microphone is working correctly";
                getSpeech(context, microphoneInfo);
                break;
            case "vibration":
                String vibrationInfo = novaUtils.isVibrationWorking(context);
                getSpeech(context, vibrationInfo);
                break;
            case "language":
                String languageInfo = novaUtils.getPrimaryLanguage(context);
                getSpeech(context, languageInfo);
                break;
            case "volume":
                VolumeControl.setStreamMusicVolume(js.optInt("volume"), context);
                getSpeech(context, "Volume set to " + js.optInt("volume"));
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
