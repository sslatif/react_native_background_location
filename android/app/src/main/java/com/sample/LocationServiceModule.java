package com.sample;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class LocationServiceModule extends ReactContextBaseJavaModule {
    LocationServiceModule(ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return "LocationServiceModule";
    }

    @ReactMethod
    public void startService() {
        Intent serviceIntent = new Intent(getReactApplicationContext(), MyLocationService.class);
        getReactApplicationContext().startService(serviceIntent);
    }

    @ReactMethod
    public void stopService() {
        Intent serviceIntent = new Intent(getReactApplicationContext(), MyLocationService.class);
        getReactApplicationContext().stopService(serviceIntent);
    }

    @ReactMethod
    public void startLocationService() {
        ContextCompat.startForegroundService(getReactApplicationContext(), new Intent(getReactApplicationContext(), MyLocationService.class));
    }

    @ReactMethod
    public void stopLocationService() {
        getReactApplicationContext().stopService(new Intent(getReactApplicationContext(), MyLocationService.class));
    }

    private void showToast(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
