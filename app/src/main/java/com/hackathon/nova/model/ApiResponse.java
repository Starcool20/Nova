package com.hackathon.nova.model;

import java.util.Base64;

public class ApiResponse {
  private String audio; // Store the Base64-encoded string temporarily
  private String transcript;
  private String response;

  // Deserialize the Base64 string into a byte array
  public byte[] getAudio() {
    if (audio != null) {
      return Base64.getDecoder().decode(audio);
    }
    return null;
  }

  public String getTranscript() {
    return transcript;
  }

  public String getResponse() {
    return response;
  }

  // Optional: A setter for audio if needed
  public void setAudio(byte[] audioBytes) {
    this.audio = Base64.getEncoder().encodeToString(audioBytes);
  }
}
