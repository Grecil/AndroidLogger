package com.example.androidlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenUnlockReceiver extends BroadcastReceiver {

    private static final String TAG = "ScreenUnlockReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            Log.d(TAG, "Screen unlocked event received.");
            
            
            EncryptedDatabaseHelper dbHelper = new EncryptedDatabaseHelper(context.getApplicationContext());
            long timestamp = System.currentTimeMillis();
            long rowId = dbHelper.insertLog(timestamp, "SCREEN_UNLOCK", null); 
            if (rowId != -1) {
                 Log.d(TAG, "Screen unlock event stored in DB with ID: " + rowId);
            } else {
                 Log.e(TAG, "Failed to store screen unlock event in DB.");
            } 
            
        }
    }
} 