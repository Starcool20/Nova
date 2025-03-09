package com.hackathon.nova.command;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.hackathon.nova.api.client.RetrofitClient;
import com.hackathon.nova.api.service.ApiService;
import com.hackathon.nova.helper.ContactListHelper;
import com.hackathon.nova.overlay.OverlayWindow;
import com.hackathon.nova.util.NovaUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Command {
    private static BroadcastReceiver myReceiver;
    private static Call<ResponseBody> call;
    private static boolean isRegistered = false;

    public static void execute(Context context, String command, JSONObject jsonObject) {
        Log.d("Command", "Executing command: " + command);

        final NovaUtils novaUtils = new NovaUtils(context);

        if (command.contains("open")) {
            String msg = novaUtils.openApp(jsonObject.optString("packageName"));
            if (!msg.equals("Pending")) {
                OverlayWindow.destroy();
            }

        } else if (command.contains("call")) {
            String phoneNumber = "";
            if (!jsonObject.optBoolean("isNumeric")) {
                phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
                if (phoneNumber.equals("Contact Not Found")) {
                    OverlayWindow.destroy();
                    showMessage("Contact Not Found", context);
                    return;
                }
            } else {
                phoneNumber = jsonObject.optString("contactName");
            }
            String msg2 = novaUtils.callContact(phoneNumber);
            if (!msg2.equals("Pending")) {
                OverlayWindow.destroy();
            }
        } else if (command.contains("set")) {
            String msg2 = novaUtils.setAlarm("NOVA ALARM", jsonObject.optInt("hour"), jsonObject.optInt("minutes"));
            if (!msg2.equals("Pending")) {
                OverlayWindow.destroy();
            }
        } else if (command.contains("play")) {
            String msg3 = novaUtils.playSong(jsonObject.optString("songName"));
            if (!msg3.equals("Pending")) {
                OverlayWindow.destroy();
            }
        } else if (command.contains("send")) {
            String phoneNumber = "";
            if (!jsonObject.optBoolean("isNumeric")) {
                phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
                Log.d("Command", "Phone Number: " + phoneNumber);
                if (phoneNumber.equals("Contact Not Found")) {
                    OverlayWindow.destroy();
                    showMessage("Contact Not Found", context);
                    return;
                }
            } else {
                phoneNumber = jsonObject.optString("contactName");
            }

            String msg4 = novaUtils.sendSMS(phoneNumber, jsonObject.optString("message"));
            Log.d("Command", "Message: " + msg4);
            if (!msg4.equals("Pending")) {
                OverlayWindow.destroy();
            }
        } else if (command.contains("email")) {
            String msg5 = novaUtils.sendEmail(context, jsonObject.optString("gmail"), "", jsonObject.optString("message"));
            if (!msg5.equals("Pending")) {
                OverlayWindow.destroy();
            }
        } else if (command.contains("whatsapp")) {
            String phoneNumber = "";
            if (!jsonObject.optBoolean("isNumeric")) {
                phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
                Log.d("Command", phoneNumber);
                if (phoneNumber.equals("Contact Not Found")) {
                    OverlayWindow.destroy();
                    showMessage("Contact Not Found", context);
                    return;
                }
            } else {
                phoneNumber = jsonObject.optString("contactName");
            }

            String msg6 = novaUtils.sendMessageToWhatsApp(phoneNumber, jsonObject.optString("message"));
            Log.d("Command", msg6);
            if (!msg6.equals("Pending")) {
                OverlayWindow.destroy();
            }
        } else if (command.contains("telegram")) {
            String phoneNumber = "";
            if (!jsonObject.optBoolean("isNumeric")) {
                phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
                Log.d("Command", phoneNumber);
                if (phoneNumber.equals("Contact Not Found")) {
                    OverlayWindow.destroy();
                    showMessage("Contact Not Found", context);
                    return;
                }
            } else {
                phoneNumber = jsonObject.optString("contactName");
            }

            String msg7 = novaUtils.sendMessageToTelegram(phoneNumber, jsonObject.optString("message"));
            if (!msg7.equals("Pending")) {
                OverlayWindow.destroy();
            }
        } else if (command.contains("check")) {
            performCheckOperation(context, jsonObject.optString("checkCommand"), novaUtils, jsonObject);
        } else {
            Log.d("Command", "Unknown command: " + command);
            OverlayWindow.showError();
            showMessage("Unknown command: " + command, context);
        }
    }

    private static void performCheckOperation(Context context, String command, NovaUtils novaUtils, JSONObject js) {
        switch (command) {
            case "battery percentage":
                String batteryInfo = novaUtils.checkBattery();
                generateAudioFile(context, batteryInfo);
                break;
            case "storage":
                String storageInfo = novaUtils.checkStorage();
                generateAudioFile(context, storageInfo);
                break;
            case "ram":
                String ramInfo = novaUtils.getRAMInfo();
                generateAudioFile(context, ramInfo);
                break;
            case "internet":
                String internetInfo = novaUtils.checkInternet();
                if (novaUtils.isInternetAvailable()) {
                    internetInfo = "Connected to Internet";
                } else {
                    internetInfo = "No internet connection";
                }
                generateAudioFile(context, internetInfo);
                break;
            case "speaker":
                String speakerInfo = "If you can hear me then speaker is working correctly";
                generateAudioFile(context, speakerInfo);
                break;
            default:
                OverlayWindow.destroy();
                Log.d("Command", "Unknown check command: " + command);
                break;
        }
    }

    public static void registerReceiverService(Context context) {
        // Define the receiver inside the activity
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                unRegisterReceiver(context);
                if (intent.getBooleanExtra("ISSUCCESS", false)) {
                    String msg = intent.getStringExtra("MESSAGE");
                    //getSpeech(context, msg);
                    OverlayWindow.destroy();
                } else {
                    String msg = intent.getStringExtra("MESSAGE");
                    // getSpeech(context, msg);
                    OverlayWindow.showError();
                }
            }
        };

        // Register the receiver
        IntentFilter filter = new IntentFilter("com.hackathon.nova.COMMAND_FOREGROUND_SERVICE");
        ContextCompat.registerReceiver(context, myReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        isRegistered = true;
        Log.d("Command", "REGISTERED");
    }

    private static void unRegisterReceiver(Context context) {
        if (myReceiver != null && isRegistered) {
            Log.d("Command", "UNREGISTERED");
            isRegistered = false;
            context.unregisterReceiver(myReceiver);
            myReceiver = null;
        }


    }

    private static void cancelNetworkRequest() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    private static void generateAudioFile(Context context, String text) {
        Retrofit retrofit = RetrofitClient.getClient();
        ApiService apiService = retrofit.create(ApiService.class);
        call = apiService.getSpeech(text);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    OverlayWindow.response();
                    processBinaryOutput(context, response);
                } else {
                    OverlayWindow.showError();
                    showMessage("Error: " + response.message(), context);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) {
                    // Handle cancellation
                    OverlayWindow.destroy();
                    showMessage("Operation cancelled prematurely", context);
                } else {
                    // Handle other types of failures
                    showMessage(
                            "Failed: " + throwable.getMessage() + ". Ensure you have a strong internet connection.", context);
                    OverlayWindow.showError();
                }
            }
        });
    }

    private static void processBinaryOutput(Context context, Response<ResponseBody> response) {
        if (response.body() == null) {
            Log.e("VoiceRecognizer", "Response body is null");
            OverlayWindow.showError();
            return;
        }

        File audioFile = new File(context.getExternalFilesDir(null), "nova.mp3");

        try (BufferedSource source = response.body().source()) {
            // Save audio data
            try (Sink sink = Okio.sink(audioFile)) {
                source.readAll(sink);
            }
            // Play audio
            play(context, audioFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e("VoiceRecognizer", "Error processing response: " + e.getMessage(), e);
            OverlayWindow.showError();
        }
    }

    private static void play(Context context, String audioFile) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                OverlayWindow.destroy();
            });
        } catch (IOException e) {
            Log.e("Command", "Error playing audio: " + e.getMessage());
            OverlayWindow.showError();
            showMessage(e.getMessage(), context);
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
            OverlayWindow.showError();
            showMessage(e.getMessage(), context);
        }
    }

    private static void showMessage(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
