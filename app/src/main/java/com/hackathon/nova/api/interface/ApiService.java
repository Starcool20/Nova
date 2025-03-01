package com.hackathon.nova.api.interfaces;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("/prompt-nova")
        // Change this to your actual endpoint
    Call<ResponseBody> transcribeVoice(
            @Part MultipartBody.Part audioFile,
            @Part("metadata") RequestBody jsonBody
    );
}