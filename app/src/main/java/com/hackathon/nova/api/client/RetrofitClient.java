package com.hackathon.nova.api.client;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
  private static final String BASE_URL =
      "https://nova-ai-server.vercel.app/"; // Base URL for OpenAI API
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configure OkHttpClient with custom timeouts
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS)
                .readTimeout(10000, TimeUnit.SECONDS)
                .writeTimeout(10000, TimeUnit.SECONDS)
                .build();

            // Initialize Retrofit with the custom OkHttpClient
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}