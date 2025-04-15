package com.example.androidlogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectActivity extends AppCompatActivity {

    private static final String TAG = "AppSelectActivity";
    private RecyclerView recyclerViewApps;
    private AppSelectAdapter adapter;
    private List<AppInfo> appInfoList;
    private Set<String> trackedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_select);

        recyclerViewApps = findViewById(R.id.recyclerViewApps);
        recyclerViewApps.setLayoutManager(new LinearLayoutManager(this));

        loadTrackedAppsPreference();
        loadInstalledApps();

        if (appInfoList != null) {
            adapter = new AppSelectAdapter(this, appInfoList, SettingsActivity.KEY_TRACKED_APPS);
            recyclerViewApps.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Could not load app list", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "App info list is null after loading.");
        }
    }

    private void loadTrackedAppsPreference() {
        trackedApps = SettingsActivity.getTrackedApps(this);
        if (trackedApps == null) {
             Log.w(TAG, "Tracked apps set was null, initializing to empty set.");
             trackedApps = new HashSet<>(); 
        }
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        appInfoList = new ArrayList<>();

        if (trackedApps == null) {
            Log.e(TAG, "Tracked apps set is null before processing packages.");
            trackedApps = new HashSet<>(); 
        }

        for (ApplicationInfo packageInfo : packages) {
            
            
                try {
                    String appName = packageInfo.loadLabel(pm).toString();
                    String packageName = packageInfo.packageName;
                    boolean isTracked = trackedApps.contains(packageName);
                    appInfoList.add(new AppInfo(
                            packageName,
                            appName,
                            packageInfo.loadIcon(pm),
                            isTracked
                    ));
                } catch (Exception e) {
                    Log.e(TAG, "Error loading info for package: " + packageInfo.packageName, e);
                }
            
        }

        
        Collections.sort(appInfoList, (o1, o2) -> o1.appName.compareToIgnoreCase(o2.appName));
    }
} 