package com.hackathon.nova.speech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonArray;
import android.icu.util.Output;
import com.hackathon.nova.MainActivity;
import com.hackathon.nova.Processes;
import com.hackathon.nova.R;
import com.hackathon.nova.audio.AudioRecorder;
import com.hackathon.nova.database.DatabaseExecutor;
import com.hackathon.nova.database.DatabaseHelper;
import com.hackathon.nova.interfaces.ApiService;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import org.vosk.LogLevel;
import org.vosk.LibVosk;
import org.vosk.android.RecognitionListener;
import org.vosk.android.StorageService;
import org.vosk.Recognizer;
import org.vosk.Model;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import retrofit2.Retrofit;
import com.hackathon.nova.client.RetrofitClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import com.hackathon.nova.overlay.OverlayWindow;
import java.io.InputStream;

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
  private static DatabaseHelper databaseHelper;
  private static ExecutorService executorService;

  public VoiceRecognizer(Context context) {
    this.context = context;
    filePath = context.getExternalFilesDir(null).getAbsolutePath(); // Ensure path is correct
    audioFile = new File(filePath, "nova.m4a");
    audioRecorder = new AudioRecorder(context);
    this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    listener = this;
    databaseHelper = new DatabaseHelper(context);
    executorService = DatabaseExecutor.getExecutor();
    LibVosk.setLogLevel(LogLevel.INFO);

    initModel();
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

  public static void registerListeners() {}

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

    call = apiService.transcribeVoice(audioPart);
    call.enqueue(
        new Callback<ResponseBody>() {
          @Override
          public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
              String contentType = response.raw().header("Content-Type");
              if ("text/plain".equalsIgnoreCase(contentType)) {
                OverlayWindow.destroy();
              } else if ("application/json".equalsIgnoreCase(contentType)) {
                handleJsonResponse(response.body().toString());
              } else {
                showMessage("Error: Internal server error");
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
            } else {
              // Handle other types of failures
              showMessage(
                  "Failed: " + t.getMessage() + ". Ensure you have a strong internet connection.");
              OverlayWindow.showError();
            }
          }
        });
  }

  private static void playAudioStream(byte[] audioData) {
    try {
      // Create a temporary file to store the streamed audio
      File tempFile = File.createTempFile("streamed_audio", "mp3", context.getCacheDir());
      tempFile.deleteOnExit();

      // Write the streamed response to the temp file
      try (InputStream inputStream = ByteArrayInputStream(audioData);
          FileOutputStream outputStream = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }

      // Use MediaPlayer to play the temp audio file
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setDataSource(tempFile.getAbsolutePath());
      mediaPlayer.prepare();
      mediaPlayer.start();

      mediaPlayer.setOnCompletionListener(
          mp -> {
            mp.release();
            tempFile.delete(); // Clean up temp file after playback
            OverlayWindow.destroy();
          });

    } catch (Exception e) {
      e.printStackTrace();
      showMessage(e.getMessage());
      OverlayWindow.showError();
    }
  }

  private static void handleJsonResponse(String responseBody) {
    JSONObject jsonObject = new JSONObject(responseBody);
    String encodedAudio = jsonObject.getString("audio");
    String transcription = jsonObject.getString("transcript");
    String responseText = jsonObject.getString("response");
    byte[] audioBytes = Base64.decode(encodedAudio, Base64.DEFAULT);

    executorService.execute(
        () -> {
          databaseHelper.insertData(
              transcription, String.valueOf(System.currentTimeMillis()) + "_transcript");
          databaseHelper.insertData(
              responseText, String.valueOf(System.currentTimeMillis()) + "_response");
        });

    playAudioStream(audioBytes);
  }

  private static void showMessage(String text) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
  }

  private static void cancelNetworkRequest() {
    if (call != null && !call.isCanceled()) {
      call.cancel();
    }
  }
}
