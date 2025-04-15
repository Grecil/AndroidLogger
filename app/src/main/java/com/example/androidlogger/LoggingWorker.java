package com.example.androidlogger;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.List;
import android.app.usage.UsageStats;
import java.util.concurrent.TimeUnit;
import java.util.Set;

public class LoggingWorker extends Worker {

    private static final String TAG = "LoggingWorker";
    private final Context applicationContext;

    public LoggingWorker(
        @NonNull Context context,
        @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.applicationContext = context.getApplicationContext();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1) 
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "LoggingWorker starting work.");

        try {
            
            UsageLogger usageLogger = new UsageLogger(applicationContext);
            List<UsageStats> stats = usageLogger.getUsageStatsLast24Hours();
            Log.d(TAG, "Fetched " + (stats != null ? stats.size() : 0) + " usage stats entries.");

            
            if (stats != null) {
                processUsageStats(stats);
            }
            
            
            

            Log.d(TAG, "LoggingWorker finished work successfully.");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error during worker execution", e);
            return Result.failure();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) 
    private void processUsageStats(List<UsageStats> stats) {
        
        Set<String> trackedApps = SettingsActivity.getTrackedApps(applicationContext);
        EncryptedDatabaseHelper dbHelper = new EncryptedDatabaseHelper(applicationContext);

        
        
        if (trackedApps.isEmpty()) {
            Log.d(TAG, "No apps selected for tracking in settings.");
            
        }

        long totalForegroundTimeMillis = 0;
        long currentTime = System.currentTimeMillis();

        for (UsageStats usageStats : stats) {
            String packageName = usageStats.getPackageName();
            
            if (trackedApps.contains(packageName)) {
                long timeInForeground = usageStats.getTotalTimeInForeground();
                if (timeInForeground > 0) {
                    totalForegroundTimeMillis += timeInForeground;
                    
                    
                    String data = packageName + "," + timeInForeground;
                    
                    long timestamp = usageStats.getLastTimeUsed(); 
                    dbHelper.insertLog(timestamp, "APP_USAGE", data);
                }
            }
        }
        
        
        long totalForegroundTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(totalForegroundTimeMillis);

        Log.d(TAG, "Total foreground usage for tracked apps in last 24h: " + totalForegroundTimeMinutes + " minutes.");

        
        int thresholdMinutes = SettingsActivity.getUsageThresholdMinutes(applicationContext);

        
        if (thresholdMinutes > 0 && totalForegroundTimeMinutes > thresholdMinutes) {
            Log.i(TAG, "Tracked app usage threshold exceeded! (" + totalForegroundTimeMinutes + "/" + thresholdMinutes + " min). Sending notification.");
            String notificationMessage = "Tracked apps used for " + totalForegroundTimeMinutes +
                                         " minutes today (limit: " + thresholdMinutes + " min).";
            
            NotificationHelper.sendThresholdExceededNotification(applicationContext, notificationMessage);
        }
    }
} 