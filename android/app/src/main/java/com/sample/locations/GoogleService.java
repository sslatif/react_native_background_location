package com.sample.locations;

import static com.sample.LocationServiceModule.reactContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.sample.R;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GoogleService extends Service implements LocationListener {
    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private final Handler mHandler = new Handler();
    long notify_interval = 1000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;

    public GoogleService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);
        //fn_getlocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "LatLong:" + location.getLatitude() + "," + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("onLocationChanged", "onStatusChanged:" + provider);

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("onLocationChanged", "onProviderEnabled:" + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("onLocationChanged", "onProviderDisabled:" + provider);
    }

    private void getCurrentLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnable && !isNetworkEnable) {
                Log.d("Location issue", "Check either GPS or Network is enabled");
            } else {
                if (isNetworkEnable) {
                    location = null;
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location == null) {
                            location = getLastKnownLocation();
                        }
                        if (location != null) {
                            Log.e("latitude", location.getLatitude() + "");
                            Log.e("longitude", location.getLongitude() + "");
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            fn_update(location);
                        }
                    }
                }

                if (isGPSEnable) {
                    location = null;
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                    if (locationManager != null) {
                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null) {
                            location = getLastKnownLocation();
                        }
                        if (location != null) {
                            Log.e("latitude", location.getLatitude() + "");
                            Log.e("longitude", location.getLongitude() + "");
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            fn_update(location);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {
            mHandler.post(() -> getCurrentLocation());
        }
    }

    private void fn_update(Location location) {
        try {
            WritableMap map = Arguments.createMap();
            DecimalFormat decimalFormat = new DecimalFormat("0.0000");
            map.putString("latitude", decimalFormat.format(location.getLatitude()));
            map.putString("longitude", decimalFormat.format(location.getLongitude()));
            // map.putString("latitude", String.valueOf(location.getLatitude()));
            // map.putString("longitude", String.valueOf(location.getLongitude()));

            map.putString("altitude", String.valueOf(location.getAltitude()));
            map.putString("accuracy", String.valueOf(location.getAccuracy()));
            map.putString("speed", String.valueOf(location.getSpeed()));
            map.putString("timestamp", String.valueOf(location.getTime()));
            sendEvent(reactContext, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("onLocationChanged", "fn_update:LatLong:" + location.getLatitude() + "," + location.getLongitude());
    }

    private void sendEvent(ReactContext reactContext, @Nullable WritableMap params) {
        try {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("location", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}