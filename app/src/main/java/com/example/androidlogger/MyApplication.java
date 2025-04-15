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
    public static final String CHANNEL_ID_THRESHOLD = "THRESHOLD_NOTIFICATIONS"; 

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase.loadLibs(this); 
        createNotificationChannel();
        schedulePeriodicLogging();
    }

    private void createNotificationChannel() {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name_threshold); 
            String description = getString(R.string.channel_description_threshold); 
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_THRESHOLD, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void schedulePeriodicLogging() {
        
        Constraints constraints = new Constraints.Builder()
                
                .build();

        
        PeriodicWorkRequest loggingWorkRequest = 
            new PeriodicWorkRequest.Builder(LoggingWorker.class, 15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            
            .build();

        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LOGGING_WORK_TAG, 
            ExistingPeriodicWorkPolicy.KEEP, 
            loggingWorkRequest
        );
    }
} 