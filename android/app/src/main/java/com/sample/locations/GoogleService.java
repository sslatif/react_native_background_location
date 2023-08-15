package com.sample.locations;

import static com.sample.LocationServiceModule.reactContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.sample.MainApplication;
import com.sample.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GoogleService extends Service implements LocationListener {
    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    double lastLatitude, lastLongitude = 0.0;
    LocationManager locationManager;
    Location location;
    private final Handler mHandler = new Handler();
    long notify_interval = 1000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;
    Timer locationTimer = new Timer();
    TimerTask locationTimerTask;

    Timer dbTimer = new Timer();
    TimerTask dbTimerTask;

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setOngoing(false).setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
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
            DecimalFormat decimalFormat = new DecimalFormat("0.0000");
            /*WritableMap map = Arguments.createMap();
            map.putString("latitude", decimalFormat.format(location.getLatitude()));
            map.putString("longitude", decimalFormat.format(location.getLongitude()));
            // map.putString("latitude", String.valueOf(location.getLatitude()));
            // map.putString("longitude", String.valueOf(location.getLongitude()));
            map.putString("altitude", String.valueOf(location.getAltitude()));
            map.putString("accuracy", String.valueOf(location.getAccuracy()));
            map.putString("speed", String.valueOf(location.getSpeed()));
            map.putString("timestamp", String.valueOf(location.getTime()));
            sendEvent(reactContext, map);
            */

            LocationData data = new LocationData();
            data.setLatitude(decimalFormat.format(location.getLatitude()));
            data.setLongitude(decimalFormat.format(location.getLongitude()));
            data.setAltitude(String.valueOf(location.getAltitude()));
            data.setAccuracy(String.valueOf(location.getAccuracy()));
            data.setSpeed(String.valueOf(location.getSpeed()));
            data.setTimestamp(String.valueOf(location.getTime()));
            saveData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("onLocationChanged", "fn_update:LatLong:" + location.getLatitude() + "," + location.getLongitude());
    }

    private void saveData(LocationData data) {
        try {
            double distanceInMeter = calculationByDistance(lastLatitude, lastLongitude, Double.parseDouble(data.latitude), Double.parseDouble(data.longitude));
            if (distanceInMeter > 5) {
                lastLatitude = Double.parseDouble(data.latitude);
                lastLongitude = Double.parseDouble(data.longitude);
                long newId = MainApplication.databaseHelper.insertItem(data);
                Log.d("Database", "saveData:" + distanceInMeter + " NewRecordID:" + newId);

            } else {
                if (locationTimerTask == null) {
                    locationTimerTask = new TimerTask() {
                        public void run() {
                            Log.d("Database", "30 seconds passed....");
                            if (locationTimerTask != null) {
                                locationTimerTask.cancel();
                                locationTimerTask = null;
                                //locationTimer.purge();
                            }
                            lastLatitude = 0.0;
                            lastLongitude = 0.0;
                        }
                    };
                    locationTimer.scheduleAtFixedRate(locationTimerTask, 20000, 30000);
                }
            }
            //getDataFromDbAndSendToReact();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double calculationByDistance(double initialLat, double initialLong, double finalLat, double finalLong) {
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(finalLat - initialLat);
        double dLon = toRadians(finalLong - initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Log.d("Difference", "Dist:" + R * c);
        return R * c;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI / 180);
    }

    private void getDataFromDbAndSendToReact() {
        try {
            if (dbTimerTask == null) {
                dbTimerTask = new TimerTask() {
                    public void run() {
                        Log.d("Database", "Send data to React-native");
                        if (dbTimerTask != null) {
                            dbTimerTask.cancel();
                            dbTimerTask = null;
                        }
                        Cursor cursor = MainApplication.databaseHelper.getAllItems();
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                String timeStamps = cursor.getString(1);
                                String latitude = cursor.getString(2);
                                String longitude = cursor.getString(3);
                                String altitude = cursor.getString(4);
                                String accuracy = cursor.getString(5);
                                String speed = cursor.getString(6);
                                Log.d("Database", "timeStamps: " + timeStampToDateTime(Long.parseLong(timeStamps)) + " Lat: "
                                        + latitude + " Long:" + longitude + " Alt:" + altitude + " Accur:" + accuracy + " Speed:" + speed);

                                // Send data to react-native app
                                WritableMap map = Arguments.createMap();
                                map.putString("latitude", String.valueOf(location.getLatitude()));
                                map.putString("longitude", String.valueOf(location.getLongitude()));
                                map.putString("altitude", String.valueOf(location.getAltitude()));
                                map.putString("accuracy", String.valueOf(location.getAccuracy()));
                                map.putString("speed", String.valueOf(location.getSpeed()));
                                map.putString("timestamp", String.valueOf(location.getTime()));
                                sendEvent(reactContext, map);
                            }
                        }
                    }
                };
                dbTimer.scheduleAtFixedRate(dbTimerTask, 10000, 10000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEvent(ReactContext reactContext, @Nullable WritableMap params) {
        try {
            /*LocationData data = new LocationData();
            data.setLatitude(params.getString("latitude"));
            data.setLongitude(params.getString("longitude"));
            data.setAltitude(params.getString("altitude"));
            data.setAccuracy(params.getString("accuracy"));
            data.setSpeed(params.getString("speed"));
            data.setTimestamp(params.getString("timestamp"));
            saveData(data);*/
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("location", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}