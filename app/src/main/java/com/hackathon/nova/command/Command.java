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
import com.hackathon.nova.service.NovaAccessibilityService;
import com.hackathon.nova.util.NovaUtils;
import com.hackathon.nova.volume.VolumeControl;

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

    public static void execute(Context context, String command, JSONObject jsonObject) {
        Log.d("Command", "Executing command: " + command);

        final NovaUtils novaUtils = new NovaUtils(context);

        if (command.contains("open")) {
            String msg = novaUtils.openApp(jsonObject.optString("packageName"));
            if (!msg.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg);
            }

        } else if (command.contains("call")) {
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }
            String msg2 = novaUtils.callContact(phoneNumber);
            if (!msg2.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg2);
            }
        } else if (command.contains("set")) {
            String msg2 = novaUtils.setAlarm("NOVA ALARM", jsonObject.optInt("hour"), jsonObject.optInt("minutes"));
            if (!msg2.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg2);
            }
        } else if (command.contains("play")) {
            String msg3 = novaUtils.playSong(jsonObject.optString("songName"));
            if (!msg3.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg3);
            }
        } else if (command.contains("send")) {
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            Log.d("Command", "Phone Number: " + phoneNumber);
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }

            String msg4 = novaUtils.sendSMS(phoneNumber, jsonObject.optString("message"));
            Log.d("Command", "Message: " + msg4);
            if (!msg4.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg4);
            }
        } else if (command.contains("email")) {
            String msg5 = novaUtils.sendEmail(context, jsonObject.optString("gmail"), "", jsonObject.optString("message"));
            if (!msg5.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg5);
            }
        } else if (command.contains("whatsapp")) {
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }

            String msg6 = novaUtils.sendMessageToWhatsApp(phoneNumber, jsonObject.optString("message"));
            if (!msg6.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg6);
            }
        } else if (command.contains("telegram")) {
            String phoneNumber = ContactListHelper.fetchContactByName(context, jsonObject.optString("contactName"));
            if (phoneNumber.equals("Contact Not Found")) {
                getSpeech(context, "Contact Not Found");
                return;
            }

            String msg7 = novaUtils.sendMessageToTelegram(phoneNumber, jsonObject.optString("message"));
            if (!msg7.equals("Pending")) {
                OverlayWindow.destroy();
                getSpeech(context, msg7);
            }
        } else if (command.contains("go")) {
            new NovaAccessibilityService().goHome();
            Log.d("Command", "Going Home");
        } else if (command.contains("check")) {
            performCheckOperation(context, command, novaUtils, jsonObject);
        } else {
            Log.d("Command", "Unknown command: " + command);
        }
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
                if (intent.getBooleanExtra("ISSUCCESS", false)) {
                    String msg = intent.getStringExtra("MESSAGE");
                    getSpeech(context, msg);
                } else {
                    String msg = intent.getStringExtra("MESSAGE");
                    getSpeech(context, msg);
                }
                OverlayWindow.destroy();
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

        cancelNetworkRequest();
    }

    private static void cancelNetworkRequest() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    private static void getSpeech(Context context, String text) {
        if (text != null) {
            if (text.contains("App not found")) {
                playAudio(context, "nova_app_not_found");
            } else if (text.contains("Success")) {
                playAudio(context, "nova_done");
            } else if (text.contains("Ongoing")) {
                playAudio(context, "error");
            }
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

        File audioFile = new File(context.getExternalFilesDir(null), "streamed_audio.mp3");

        try (BufferedSource source = response.body().source()) {
            // Read metadata (first line)
            JSONObject metadata = new JSONObject(source.readUtf8LineStrict());
            // Save audio data
            try (Sink sink = Okio.sink(audioFile)) {
                source.readAll(sink);
            }
            // Play audio
            playAudio(context, audioFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e("VoiceRecognizer", "Error processing response: " + e.getMessage(), e);
            OverlayWindow.showError();
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
