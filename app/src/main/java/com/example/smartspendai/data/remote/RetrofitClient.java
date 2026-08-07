package com.example.smartspendai.data.remote;

import android.content.Context;

import com.example.smartspendai.data.local.TokenManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 10.0.2.2 is a special address meaning "my computer's localhost",
    // but ONLY as seen from inside the Android EMULATOR.
    //
    // Testing on a REAL phone instead? Replace this with your laptop's
    // local network IP (e.g. "http://192.168.1.5:8000/") and make sure
    // your phone + laptop are on the same Wi-Fi network.
    //private static final String BASE_URL = "http://127.0.0.1:8000/";
    private static final String BASE_URL = "https://smartspendai-backend-1.onrender.com/";

    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = new TokenManager(context.getApplicationContext());

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // prints full request/response to Logcat — great for debugging

            // Automatically attaches "Authorization: Bearer <token>" to every
            // outgoing request IF we have a saved login session. Endpoints
            // that don't need auth (like /auth/login) simply ignore the
            // extra header — so we don't need two separate Retrofit clients.
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                String token = tokenManager.getToken();
                if (token == null) {
                    return chain.proceed(original);
                }
                Request authorized = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(authorized);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
