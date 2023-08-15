package com.sample;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.sample.locations.GoogleService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationServiceModule extends ReactContextBaseJavaModule {
    private int listenerCount = 0;
    public static ReactContext reactContext;
    private SQLiteDatabase db;

    LocationServiceModule(ReactApplicationContext context) {
        super(context);
        try {
            reactContext = context;
            SQLiteOpenHelper dbHelper = new DatabaseHelper(reactContext);
            db = dbHelper.getReadableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void fetchItems(Callback successCallback, Callback errorCallback) {
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM Location", null);
            WritableArray itemsArray = new WritableNativeArray();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    WritableMap itemMap = new WritableNativeMap();
                    String timeStamps = cursor.getString(1);
                    String latitude = cursor.getString(2);
                    String longitude = cursor.getString(3);
                    String altitude = cursor.getString(4);
                    String accuracy = cursor.getString(5);
                    String speed = cursor.getString(6);

                    itemMap.putString("timeStamps", timeStampToDateTime(Long.parseLong(timeStamps)));
                    itemMap.putString("latitude", latitude);
                    itemMap.putString("longitude", longitude);
                    itemMap.putString("altitude", altitude);
                    itemMap.putString("accuracy", accuracy);
                    itemMap.putString("speed", speed);
                    itemsArray.pushMap(itemMap);
                } while (cursor.moveToNext());
                cursor.close();
            }
            successCallback.invoke(itemsArray);
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "LocationServiceModule";
    }

    @ReactMethod
    public void startService() {
        Intent serviceIntent = new Intent(getReactApplicationContext(), GoogleService.class);
        getReactApplicationContext().startService(serviceIntent);
    }

    @ReactMethod
    public void stopService() {
        Intent serviceIntent = new Intent(getReactApplicationContext(), GoogleService.class);
        getReactApplicationContext().stopService(serviceIntent);
    }

    @ReactMethod
    public void startLocationService() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            startService();
        else
            ContextCompat.startForegroundService(getReactApplicationContext(), new Intent(getReactApplicationContext(), GoogleService.class));
    }

    @ReactMethod
    public void stopLocationService() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            stopService();
        else
            getReactApplicationContext().stopService(new Intent(getReactApplicationContext(), GoogleService.class));
    }

    @ReactMethod
    public void addListener(String eventName) {
        if (listenerCount == 0) {
            Log.d("LocationService", "addListener: " + eventName);
            // Set up any upstream listeners or background tasks as necessary
        }

        listenerCount += 1;
    }

    private String timeStampToDateTime(long timestamp) {
        try {
            // Create a SimpleDateFormat object with the desired format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // Convert the timestamp to a Date object
            Date date = new Date(timestamp);
            // Format the Date object to the desired date-time format
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
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
