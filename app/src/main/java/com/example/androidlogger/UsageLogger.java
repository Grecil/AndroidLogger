package com.example.androidlogger;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import net.sqlcipher.database.SQLiteDatabase;

public class UsageLogger {

    private final UsageStatsManager usageStatsManager;
    private final Context context;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public UsageLogger(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (this.usageStatsManager == null) {
            // Handle error appropriately, e.g., throw an exception or log an error
            throw new RuntimeException("UsageStatsManager not available on this device.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<UsageStats> getUsageStatsLast24Hours() {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR, -24);
        long startTime = calendar.getTimeInMillis();

        // Query usage stats for the last 24 hours
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        // Filter out stats with no foreground time (optional, but often useful)
        if (stats != null) {
            return stats.stream()
                    .filter(usageStats -> usageStats.getTotalTimeInForeground() > 0)
                    .collect(Collectors.toList());
        } else {
             return java.util.Collections.emptyList(); // Return an empty list if stats are null
        }
    }
} 