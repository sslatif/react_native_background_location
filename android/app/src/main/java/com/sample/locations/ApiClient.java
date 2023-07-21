package com.sample.locations;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public interface ApiClient {
    @GET("updateLocation.json")
    Call<LocationResponse> updateLocation();

    class Companion {
        private static Retrofit retrofit = null;
        private static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

        public static Retrofit getInstance(Context context) {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl("https://www.howtodoandroid.com/apis/")
                        .client(new OkHttpClient.Builder().addInterceptor(logging).build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }
    }
}

