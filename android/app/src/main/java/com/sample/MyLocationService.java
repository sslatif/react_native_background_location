package com.sample;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

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
        String NOTIFICATION_CHANNEL_ID = "my_notification_location";
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
                Toast.makeText(
                        getApplicationContext(),
                        "Location: " + location.getLatitude() + "," + location.getLongitude(),
                        Toast.LENGTH_SHORT
                ).show();
                Log.d(TAG, "onLocationChanged: Latitude " + mLocation.getLatitude() +
                        " , Longitude " + mLocation.getLongitude());

                AppExecutors.getInstance().networkIO().execute(() -> {
                    ApiClient apiClient = ApiClient.Companion.getInstance(MyLocationService.this)
                            .create(ApiClient.class);
                    Call<LocationResponse> responseCall = apiClient.updateLocation();
                    responseCall.enqueue(new Callback<LocationResponse>() {
                        @Override
                        public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                            Log.d(TAG, "run: Running = Location Update Successful");
                        }

                        @Override
                        public void onFailure(Call<LocationResponse> call, Throwable t) {
                            Log.d(TAG, "run: Running = Location Update Failed");
                        }
                    });
                });
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceStarted = false;
    }
}

