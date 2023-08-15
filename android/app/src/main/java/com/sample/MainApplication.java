package com.sample;

import android.app.Application;
import android.database.Cursor;
import android.util.Log;

import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactNativeHost;
import com.facebook.soloader.SoLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainApplication extends Application implements ReactApplication {
    public static DatabaseHelper databaseHelper;
    private final ReactNativeHost mReactNativeHost = new DefaultReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            @SuppressWarnings("UnnecessaryLocalVariable") List<ReactPackage> packages = new PackageList(this).getPackages();
            // Packages that cannot be autolinked yet can be added manually here, for example:
            // packages.add(new MyReactNativePackage());
            packages.add(new NativeModulesPackage());
            return packages;
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }

        @Override
        protected boolean isNewArchEnabled() {
            return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
        }

        @Override
        protected Boolean isHermesEnabled() {
            return BuildConfig.IS_HERMES_ENABLED;
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            DefaultNewArchitectureEntryPoint.load();
        }
        initializeDatabase();
        ReactNativeFlipper.initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
    }

    public void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
        Log.d("Database", "initializeDatabase & get Records");
        Cursor cursor = databaseHelper.getAllItems();
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
            }
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

    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
