package com.example.smartspendai.data.remote;

import com.example.smartspendai.data.model.LoginRequest;
import com.example.smartspendai.data.model.RegisterRequest;
import com.example.smartspendai.data.model.TokenResponse;
import com.example.smartspendai.data.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("auth/register")
    Call<UserResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<TokenResponse> login(@Body LoginRequest request);
}
