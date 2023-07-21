package com.sample.locations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.sample.MyLocationService;

public class BootDeviceReceivers extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null) {
            ContextCompat.startForegroundService(context, new Intent(context, MyLocationService.class));
        }
    }
}