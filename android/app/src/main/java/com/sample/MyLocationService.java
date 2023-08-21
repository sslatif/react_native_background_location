package com.sample;

import static com.sample.LocationServiceModule.reactContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.sample.locations.ApiClient;
import com.sample.locations.AppExecutors;
import com.sample.locations.LocationHelper;
import com.sample.locations.LocationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLocationService extends Service {

    static Location mLocation;
    static boolean isServiceStarted = false;

    private final String TAG = "LocationService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceStarted = true;
        String NOTIFICATION_CHANNEL_ID = "Location App";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(false)
                .setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_ID);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
            startForeground(1, builder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocationHelper locationHelper = new LocationHelper();
        locationHelper.startListeningUserLocation(this, location -> {
            mLocation = location;
            if (mLocation != null) {
                Log.d(TAG, "onLocationChanged: Latitude " + mLocation.getLatitude() +
                        " , Longitude " + mLocation.getLongitude());
                try {
                    WritableMap map = Arguments.createMap();
                    map.putString("latitude", String.valueOf(mLocation.getLatitude()));
                    map.putString("longitude", String.valueOf(mLocation.getLongitude()));
                    map.putString("altitude", String.valueOf(mLocation.getAltitude()));
                    map.putString("accuracy", String.valueOf(mLocation.getAccuracy()));
                    map.putString("speed", String.valueOf(mLocation.getSpeed()));
                    map.putString("timestamp", String.valueOf(mLocation.getTime()));
                    sendEvent(reactContext, map);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AppExecutors.getInstance().networkIO().execute(() -> {
                    ApiClient apiClient = ApiClient.Companion.getInstance(MyLocationService.this)
                            .create(ApiClient.class);
                    Call<LocationResponse> responseCall = apiClient.updateLocation();
                    responseCall.enqueue(new Callback<LocationResponse>() {
                        @Override
                        public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                            Log.d(TAG, "Running = Location Update Successful:" + response.message());
                        }

                        @Override
                        public void onFailure(Call<LocationResponse> call, Throwable t) {
                            Log.d(TAG, "Running = Location Update Failed:" + t.getLocalizedMessage());
                        }
                    });
                });
            }
        });
        return START_STICKY;
    }

    private void sendEvent(ReactContext reactContext, @Nullable WritableMap params) {
        try {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("location", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceStarted = false;
    }
}

