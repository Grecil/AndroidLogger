package com.example.androidlogger;

import android.graphics.drawable.Drawable;

public class AppInfo {
    String packageName;
    String appName;
    Drawable icon;
    boolean isTracked;

    public AppInfo(String packageName, String appName, Drawable icon, boolean isTracked) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.isTracked = isTracked;
    }
} 