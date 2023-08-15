package com.sample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sample.locations.LocationData;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MapData.db";
    private static final String TABLE_NAME = "Location";
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create your tables here using SQL queries
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT,timestamp TEXT, latitude TEXT, longitude TEXT, altitude TEXT, accuracy TEXT, speed TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades, if needed
    }

    public long insertItem(LocationData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("timestamp", data.getTimestamp());
        values.put("latitude", data.getLatitude());
        values.put("longitude", data.getLongitude());
        values.put("altitude", data.getAltitude());
        values.put("accuracy", data.getAccuracy());
        values.put("speed", data.getSpeed());

        Log.d("Database", "Record Inserted");
        return db.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllItems() {
        Log.d("Database", "getAllItems");
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public int updateItem(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        return db.update(TABLE_NAME, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    public int deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "_id < ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        int deletedRows = db.delete(TABLE_NAME, whereClause, whereArgs);
        if (deletedRows > 0) {
            Log.d("Database", "Old data deleted successfully");
        } else {
            Log.d("Database", "Deletion failed or no rows matched the criteria");
        }

        //db.close();
        return deletedRows;//db.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        //db.close();
    }
}
