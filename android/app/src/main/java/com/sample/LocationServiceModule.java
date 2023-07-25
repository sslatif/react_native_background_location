package com.sample;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class LocationServiceModule extends ReactContextBaseJavaModule {
    private int listenerCount = 0;
    public static ReactContext reactContext;

    LocationServiceModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
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

    @ReactMethod
    public void addListener(String eventName) {
        if (listenerCount == 0) {
            Log.d("LocationService", "addListener: " + eventName);
            // Set up any upstream listeners or background tasks as necessary
        }

        listenerCount += 1;
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        listenerCount -= count;
        if (listenerCount == 0) {
            Log.d("LocationService", "removeListeners");
            // Remove upstream listeners, stop unnecessary background tasks
        }
    }

    private void showToast(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
