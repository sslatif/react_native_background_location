package com.sample.locations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHelper {

    @SuppressLint("MissingPermission")
    public void startListeningUserLocation(Context context, MyLocationListener myListener) {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myListener.onLocationChanged(location); // calling listener to inform that updated location is available
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        // 3 seconds. The Minimum Time to get location update
        int LOCATION_REFRESH_TIME = 3000;
        // 0 meters. The Minimum Distance to be changed to get location update
        int LOCATION_REFRESH_DISTANCE = 0;
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE,
                locationListener
        );
    }
}

