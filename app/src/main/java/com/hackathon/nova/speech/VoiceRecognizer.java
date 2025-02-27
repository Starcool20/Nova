package com.hackathon.nova.speech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.hackathon.nova.MainActivity;
import com.hackathon.nova.R;
import com.hackathon.nova.api.client.RetrofitClient;
import com.hackathon.nova.api.interfaces.ApiService;
import com.hackathon.nova.audio.AudioRecorder;
import com.hackathon.nova.database.Data;
import com.hackathon.nova.database.DatabaseHelper;
import com.hackathon.nova.database.ExecutorThread;
import com.hackathon.nova.overlay.OverlayWindow;
import com.hackathon.nova.preference.PreferenceUtil;
import com.hackathon.nova.volume.VolumeControl;

import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VoiceRecognizer implements RecognitionListener {
    public static final String TAG = "VoiceRecognizer";
    private static SpeechRecognizer speechRecognizer;
    private static Context context;
    private static Intent speechRecognizerIntent;
    private static AudioRecorder audioRecorder;
    private static AudioManager audioManager;
    private static int recordingCount = 0;
    private static String fileName = "";
    // private static String apiUrl = "https://api.openai.com/v1/audio/transcriptions";
    private static String filePath;
    private static ApiService apiService;
    private static MediaPlayer mediaPlayer;
    private static File audioFile;
    private static Call<ResponseBody> call;
    private static Model model;
    private static SpeechService speechService;
    private static SpeechStreamService speechStreamService;
    private static RecognitionListener listener;
    private static boolean stop = false;
    private static boolean saveResponse = false;

    public VoiceRecognizer(Context context) {
        this.context = context;
        filePath = context.getExternalFilesDir(null).getAbsolutePath(); // Ensure path is correct
        audioFile = new File(filePath, "nova.m4a");
        audioRecorder = new AudioRecorder(context);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        listener = this;
        LibVosk.setLogLevel(LogLevel.INFO);

        initModel();
    }

    public static void registerListeners() {
    }

    public static void startListening() {
        stop = false;
        if (speechService == null) {
            try {
                Recognizer rec = new Recognizer(model, 44100.0f);
                rec.setPartialWords(false);
                speechService = new SpeechService(rec, 44100.0f);
                speechService.startListening(listener);
                showMessage("Listening");
                MainActivity.result();
            } catch (IOException e) {
                showMessage(e.getMessage());
            }
        } else {
            speechService.startListening(listener);
        }
    }

    public static void playFeedbackSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.feedback_listen);

        try {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(
                    mp -> {
                        mp.release();
                        audioRecorder.startRecording("nova.m4a");
                    });

        } catch (Exception e) {
            mediaPlayer.stop();
            mediaPlayer.release();
            e.printStackTrace();
            showMessage(e.getMessage());
        }
    }

    public static void deleteAudioFile() {
        audioFile.delete();
    }

    public static void stopListening() {

        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
            speechStreamService = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private static void processText(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            String value = jsonObject.optString("partial", "text");
            if ("nova".equalsIgnoreCase(value) && stop == false) {
                stop = true;
                if (speechService != null) {
                    speechService.stop();
                }
                OverlayWindow.showOverlay();
            }
        } catch (Exception e) {
            showMessage(e.getMessage());
        }
    }

    public static void transcribeVoice() {
        filePath = context.getExternalFilesDir(null).getAbsolutePath(); // Ensure path is correct
        audioFile = new File(filePath, "nova.m4a");

        Retrofit retrofit = RetrofitClient.getClient();
        ApiService apiService = retrofit.create(ApiService.class);

        // Prepare the audio file as a RequestBody
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/mpeg"), audioFile);
        MultipartBody.Part audioPart =
                MultipartBody.Part.createFormData("audio", audioFile.getName(), requestFile);

        final DatabaseHelper databaseHelper = new DatabaseHelper(context);

        // Get database saved data history and append to the json
        List<Data> dataList = databaseHelper.getAllData();
        databaseHelper.closeDatabase();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user0", "");
        jsonObject.addProperty("user0_response", "");
        jsonObject.addProperty("user1", "");
        jsonObject.addProperty("user1_response", "");
        jsonObject.addProperty("user2", "");
        jsonObject.addProperty("user2_response", "");
        jsonObject.addProperty("user3", "");
        jsonObject.addProperty("user3_response", "");
        jsonObject.addProperty("user4", "");
        jsonObject.addProperty("user4_response", "");
        jsonObject.addProperty("user5", "");
        jsonObject.addProperty("user5_response", "");
        jsonObject.addProperty("user6", "");
        jsonObject.addProperty("user6_response", "");
        jsonObject.addProperty("user7", "");
        jsonObject.addProperty("user7_response", "");
        jsonObject.addProperty("user8", "");
        jsonObject.addProperty("user8_response", "");
        jsonObject.addProperty("user9", "");
        jsonObject.addProperty("user9_response", "");

        if (dataList != null && !dataList.isEmpty()) {
            for (Data data : dataList) {
                if (data.getKey().endsWith("_transcript")) {
                    jsonObject.addProperty("user" + data.getKey().substring(4, 5), data.getName());
                }

                if (data.getKey().endsWith("_response")) {
                    jsonObject.addProperty(
                            "user" + data.getKey().substring(4, 5) + "_response", data.getName());
                }
            }
            jsonObject.addProperty("count", String.valueOf(dataList.size()));
        } else {
            // Add the jsonObject data to null
            jsonObject.addProperty("count", "-1");
        }

        // Prepare the json request as RequestBody
        RequestBody jsonBody =
                RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        call = apiService.transcribeVoice(audioPart, jsonBody);
        call.enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            OverlayWindow.response();

                            try (BufferedSource source = response.body().source()) {
                                // Step 1: Read Metadata
                                String metadataLine = source.readUtf8LineStrict(); // Read the first line as metadata
                                JSONObject metadata = new JSONObject(metadataLine);
                                String transcript = metadata.getString("transcript");
                                String serverResponse = metadata.getString("response");
                                saveTranscriptAndResponse(transcript, serverResponse);

                                // Step 2: Prepare to Save the Audio
                                File audioFile = new File(context.getExternalFilesDir(null), "streamed_audio.mp3");
                                try (Sink sink = Okio.sink(audioFile)) {
                                    // Step 3: Write Binary Audio Data
                                    source.readAll(sink); // Write the remaining binary data to the audio file
                                }

                                Log.d("Metadata", "Transcript: " + transcript + ", Response: " + serverResponse);
                                Log.d("Audio", "Audio file saved to: " + audioFile.getAbsolutePath());

                                // Play the audio
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                                mediaPlayer.setOnCompletionListener(
                                        mp -> {
                                            // mp.release();
                                            saveResponse = false;
                                            // tempAudioFile.delete();
                                            OverlayWindow.destroy();
                                        });
                            } catch (Exception e) {
                                showMessage(e.getMessage());
                                Log.d("VoiceRecognizer", e.getMessage());
                                OverlayWindow.showError();
                            }
                        } else {
                            // Handle unsuccessful response
                            showMessage("Error: " + response.message());
                            OverlayWindow.showError();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (call.isCanceled()) {
                            // Handle cancellation
                            OverlayWindow.destroy();
                            showMessage("Operation cancelled prematurely");
                        } else {
                            // Handle other types of failures
                            showMessage(
                                    "Failed: " + t.getMessage() + ". Ensure you have a strong internet connection.");
                            OverlayWindow.showError();
                        }
                    }
                });
    }

    private static void cancelNetworkRequest() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    private static void saveTranscriptAndResponse(String transcript, String response) {
        if (saveResponse) {
            return;
        }
        saveResponse = true;

        VolumeControl.setVolumeToHalf(context);

        ExecutorThread.getExecutor()
                .execute(
                        () -> {
                            final DatabaseHelper databaseHelper = new DatabaseHelper(context);
                            // Extract data
                            String transcription = transcript;
                            String responseText = response;

                            // Save the data
                            List<Data> dataList = databaseHelper.getAllData();
                            if (dataList != null && !dataList.isEmpty()) {
                                if (dataList.size() == 2) {
                                    databaseHelper.insertData(transcription, "user1_transcript");
                                    databaseHelper.insertData(responseText, "user1_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 4) {
                                    databaseHelper.insertData(transcription, "user2_transcript");
                                    databaseHelper.insertData(responseText, "user2_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 6) {
                                    databaseHelper.insertData(transcription, "user3_transcript");
                                    databaseHelper.insertData(responseText, "user3_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 8) {
                                    databaseHelper.insertData(transcription, "user4_transcript");
                                    databaseHelper.insertData(responseText, "user4_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 10) {
                                    databaseHelper.insertData(transcription, "user5_transcript");
                                    databaseHelper.insertData(responseText, "user5_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 12) {
                                    databaseHelper.insertData(transcription, "user6_transcript");
                                    databaseHelper.insertData(responseText, "user6_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 14) {
                                    databaseHelper.insertData(transcription, "user7_transcript");
                                    databaseHelper.insertData(responseText, "user7_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 16) {
                                    databaseHelper.insertData(transcription, "user8_transcript");
                                    databaseHelper.insertData(responseText, "user8_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 18) {
                                    databaseHelper.insertData(transcription, "user9_transcript");
                                    databaseHelper.insertData(responseText, "user9_response");
                                    databaseHelper.closeDatabase();
                                } else if (dataList.size() == 20) {
                                    // Update and overwrite to database accordingly
                                    updateDB(databaseHelper, transcription, responseText);
                                }
                            } else {
                                databaseHelper.insertData(transcription, "user0_transcript");
                                databaseHelper.insertData(responseText, "user0_response");
                                databaseHelper.closeDatabase();
                            }
                        });
    }

    private static void updateDB(DatabaseHelper helper, String transcription, String text) {
        PreferenceUtil preference = new PreferenceUtil(context);

        if (preference.getInt("db_num", 0) == 0) {
            helper.updateData(1, transcription, "user0_transcript");
            helper.updateData(1, text, "user0_response");

            preference.saveInt("db_num", 1);
        } else if (preference.getInt("db_num", 0) == 1) {
            helper.updateData(2, transcription, "user1_transcript");
            helper.updateData(2, text, "user1_response");

            preference.saveInt("db_num", 2);
        } else if (preference.getInt("db_num", 0) == 2) {
            helper.updateData(3, transcription, "user2_transcript");
            helper.updateData(3, text, "user2_response");

            preference.saveInt("db_num", 3);
        } else if (preference.getInt("db_num", 0) == 3) {
            helper.updateData(4, transcription, "user3_transcript");
            helper.updateData(4, text, "user3_response");

            preference.saveInt("db_num", 4);
        } else if (preference.getInt("db_num", 0) == 4) {
            helper.updateData(5, transcription, "user4_transcript");
            helper.updateData(5, text, "user4_response");

            preference.saveInt("db_num", 5);
        } else if (preference.getInt("db_num", 0) == 5) {
            helper.updateData(6, transcription, "user5_transcript");
            helper.updateData(6, text, "user5_response");

            preference.saveInt("db_num", 6);
        } else if (preference.getInt("db_num", 0) == 6) {
            helper.updateData(7, transcription, "user6_transcript");
            helper.updateData(7, text, "user6_response");

            preference.saveInt("db_num", 7);
        } else if (preference.getInt("db_num", 0) == 7) {
            helper.updateData(8, transcription, "user7_transcript");
            helper.updateData(8, text, "user7_response");

            preference.saveInt("db_num", 8);
        } else if (preference.getInt("db_num", 0) == 8) {
            helper.updateData(9, transcription, "user8_transcript");
            helper.updateData(9, text, "user8_response");

            preference.saveInt("db_num", 9);
        } else if (preference.getInt("db_num", 0) == 9) {
            helper.updateData(10, transcription, "user9_transcript");
            helper.updateData(10, text, "user9_response");

            preference.saveInt("db_num", 0);
        }

        helper.closeDatabase();
    }

    public static void showMessage(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    private void initModel() {
        StorageService.unpack(
                context,
                "model-en-us",
                "model",
                (model) -> {
                    this.model = model;
                    startListening();
                },
                (exception) -> showMessage("Failed to unpack the model" + exception.getMessage()));
    }

    public void shutdown() {
        cancelNetworkRequest();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
            speechStreamService = null;
        }

        audioRecorder.stopRecording(0);
    }

    @Override
    public void onResult(String hypothesis) {
        processText(hypothesis);
    }

    @Override
    public void onFinalResult(String hypothesis) {
        processText(hypothesis);
    }

    @Override
    public void onPartialResult(String hypothesis) {
        processText(hypothesis);
    }

    @Override
    public void onError(Exception e) {
        showMessage(e.getMessage());
    }

    @Override
    public void onTimeout() {
        showMessage("Timeout");
    }
}
