package com.example.androidlogger;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    private static final String LOGGING_WORK_TAG = "LoggingWork";
    public static final String CHANNEL_ID_THRESHOLD = "THRESHOLD_NOTIFICATIONS"; // Public channel ID

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase.loadLibs(this); // Load SQLCipher libraries
        createNotificationChannel();
        schedulePeriodicLogging();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name_threshold); // Define in strings.xml
            String description = getString(R.string.channel_description_threshold); // Define in strings.xml
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_THRESHOLD, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void schedulePeriodicLogging() {
        // Define constraints (optional)
        Constraints constraints = new Constraints.Builder()
                // .setRequiredNetworkType(NetworkType.CONNECTED) // Example constraint
                .build();

        // Create a periodic work request to run every 15 minutes
        PeriodicWorkRequest loggingWorkRequest = 
            new PeriodicWorkRequest.Builder(LoggingWorker.class, 15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            // .setInitialDelay(5, TimeUnit.MINUTES) // Optional: Add initial delay
            .build();

        // Enqueue the unique periodic work
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LOGGING_WORK_TAG, // Unique name for the work
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
            loggingWorkRequest
        );
    }
} 