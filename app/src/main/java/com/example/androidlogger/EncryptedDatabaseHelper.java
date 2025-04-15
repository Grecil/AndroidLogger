package com.example.androidlogger;

import android.content.ContentValues;
import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;
import java.util.Calendar;

public class EncryptedDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "EncryptedDbHelper";
    private static final String DATABASE_NAME = "logger.db";
    private static final int DATABASE_VERSION = 1;

    
    public static final String TABLE_LOGS = "logs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_EVENT_TYPE = "event_type";
    public static final String COLUMN_DATA = "data";

    
    private static final String SQL_CREATE_LOGS_TABLE = 
        "CREATE TABLE " + TABLE_LOGS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
        COLUMN_EVENT_TYPE + " TEXT NOT NULL," +
        COLUMN_DATA + " TEXT);";

    private final Context context;

    public EncryptedDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating database table: " + TABLE_LOGS);
        db.execSQL(SQL_CREATE_LOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

    
    private static final String DATABASE_PASSWORD = "YOUR_STRONG_PASSWORD";

    /**
     * Inserts a new log entry into the encrypted database.
     *
     * @param timestamp The timestamp of the event.
     * @param eventType The type of event logged (e.g., "SCREEN_UNLOCK", "APP_USAGE").
     * @param data      Additional data associated with the event (e.g., package name, duration).
     * @return the row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insertLog(long timestamp, String eventType, String data) {
        SQLiteDatabase db = null;
        long newRowId = -1;
        try {
            db = this.getWritableDatabase(DATABASE_PASSWORD); 
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_TIMESTAMP, timestamp);
            values.put(COLUMN_EVENT_TYPE, eventType);
            values.put(COLUMN_DATA, data); 

            newRowId = db.insert(TABLE_LOGS, null, values);
            if (newRowId == -1) {
                 Log.e(TAG, "Error inserting log entry.");
            } else {
                 Log.d(TAG, "Inserted log entry with ID: " + newRowId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting writable database or inserting log", e);
        } finally {
             if (db != null && db.isOpen()) {
                 db.close();
             }
        }
        return newRowId;
    }
    
    
    

    /**
     * Queries the number of screen unlock events for a specific day.
     *
     * @param year   The year.
     * @param month  The month (1-12).
     * @param day    The day of the month.
     * @return The count of screen unlock events for that day.
     */
    public int getScreenUnlocksForDay(int year, int month, int day) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int count = 0;
        try {
            db = this.getReadableDatabase(DATABASE_PASSWORD);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, day, 0, 0, 0); 
            calendar.set(Calendar.MILLISECOND, 0);
            long startTime = calendar.getTimeInMillis();

            calendar.set(year, month - 1, day, 23, 59, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            long endTime = calendar.getTimeInMillis();

            String selection = COLUMN_EVENT_TYPE + " = ? AND " + 
                               COLUMN_TIMESTAMP + " >= ? AND " + 
                               COLUMN_TIMESTAMP + " <= ?";
            String[] selectionArgs = { "SCREEN_UNLOCK", String.valueOf(startTime), String.valueOf(endTime) };

            cursor = db.query(
                    TABLE_LOGS,
                    new String[]{"COUNT(" + COLUMN_ID + ")"}, 
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0); 
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying screen unlocks for day", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        Log.d(TAG, "Screen unlocks for " + year + "-" + month + "-" + day + ": " + count);
        return count;
    }

    /**
     * Queries the total usage time (in milliseconds) for tracked apps for a specific day.
     *
     * @param year   The year.
     * @param month  The month (1-12).
     * @param day    The day of the month.
     * @return The total usage time in milliseconds for tracked apps on that day.
     */
    public long getTotalUsageTimeForDay(int year, int month, int day) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        long totalTime = 0;
        try {
            db = this.getReadableDatabase(DATABASE_PASSWORD);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, day, 0, 0, 0); 
            calendar.set(Calendar.MILLISECOND, 0);
            long startTime = calendar.getTimeInMillis();

            calendar.set(year, month - 1, day, 23, 59, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            long endTime = calendar.getTimeInMillis();

            String selection = COLUMN_EVENT_TYPE + " = ? AND " +
                               COLUMN_TIMESTAMP + " >= ? AND " +
                               COLUMN_TIMESTAMP + " <= ?";
            String[] selectionArgs = { "APP_USAGE", String.valueOf(startTime), String.valueOf(endTime) };

            cursor = db.query(
                    TABLE_LOGS,
                    new String[]{COLUMN_DATA}, 
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA));
                    if (data != null) {
                         try {
                             
                             String[] parts = data.split(",");
                             if (parts.length == 2) {
                                 totalTime += Long.parseLong(parts[1]); 
                             }
                         } catch (NumberFormatException e) {
                             Log.w(TAG, "Could not parse duration from data: " + data);
                         } catch (Exception e) {
                             Log.e(TAG, "Error processing usage data: " + data, e);
                         }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying total usage time for day", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        Log.d(TAG, "Total usage for " + year + "-" + month + "-" + day + ": " + totalTime + " ms");
        return totalTime;
    }
} 