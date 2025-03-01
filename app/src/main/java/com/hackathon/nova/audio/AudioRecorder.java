package com.hackathon.nova.audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.hackathon.nova.overlay.OverlayWindow;
import com.hackathon.nova.speech.VoiceRecognizer;
import com.hackathon.nova.volume.VolumeControl;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioRecorder {
    public static int volume = 0;
    // private ArrayList<ExecutorService> executorList = new ArrayList<>(); // List to store all
    // instances
    private static MediaRecorder mediaRecorder;
    private final Context context;
    private ScheduledExecutorService executorService;
    private boolean stop = false;
    private int calledCount = 0;
    private boolean isSpeechDetected = false;

    public AudioRecorder(Context context) {
        this.context = context;
    }

    // Start recording and prepare MediaRecorder instance
    public void startRecording(String fileName) {
        try {
            File file = new File(context.getExternalFilesDir(null).getAbsolutePath(), fileName);

            volume = VolumeControl.getStreamMusicVolume(context);
            VolumeControl.setStreamMusicVolume(0, context);

            mediaRecorder = new MediaRecorder();

            // Use VOICE_RECOGNITION to improve speech capture with noise reduction
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128230); // High quality
            mediaRecorder.setAudioSamplingRate(44100); // CD quality
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();

            // Initialize ExecutorService
            executorService = Executors.newSingleThreadScheduledExecutor();

            // Schedule `detectVoice` task every 100ms
            executorService.scheduleWithFixedDelay(this::detectVoice, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            stopAllRecordings();
            OverlayWindow.showError();
            Toast.makeText(context, "Failed to start nova: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Stop the recording and release resources
    public void stopRecording(int index) {
        stop = true;
        calledCount = 0;
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder = null;
        }
        VolumeControl.setStreamMusicVolume(volume, context);
        shutDownExecutor();
    }

    // Static method to stop all active recordings and clear the list
    public void stopAllRecordings() {
        stop = true;
        calledCount = 0;
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder = null;
        }
        VolumeControl.setStreamMusicVolume(volume, context);
        shutDownExecutor();
    }

    private void detectVoice() {
        if (mediaRecorder != null) {
            int amplitude = mediaRecorder.getMaxAmplitude();
            onAmplitudeChanged(amplitude);
        }
    }

    private void onAmplitudeChanged(int amplitude) {
        Log.d("Amplitude", String.valueOf(amplitude));
        if (amplitude < 4000 && calledCount == 15) {
            stopRecording(0);

            if (isSpeechDetected) {
                new Handler(Looper.getMainLooper())
                        .post(
                                () -> {
                                    // Run on UI thread
                                    isSpeechDetected = false;
                                    OverlayWindow.processingAudio();
                                    // This will send the audio file to the server for adequate processing and
                                    VoiceRecognizer.transcribeVoice();
                                });
                return;
            }
            OverlayWindow.destroy();
            VoiceRecognizer.deleteAudioFile();

            return;
        }
        if (amplitude < 3500) {
            isSpeechDetected = true;
            calledCount = calledCount + 1;
        } else {
            calledCount = 0;
        }
    }

    private void shutDownExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}
