package com.example.androidlogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

public class AppSelectAdapter extends RecyclerView.Adapter<AppSelectAdapter.ViewHolder> {

    private final List<AppInfo> appList;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final String prefsKeyTrackedApps;

    public AppSelectAdapter(Context context, List<AppInfo> appList, String prefsKeyTrackedApps) {
        this.context = context;
        this.appList = appList;
        this.sharedPreferences = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        this.prefsKeyTrackedApps = prefsKeyTrackedApps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = appList.get(position);
        holder.appName.setText(appInfo.appName);
        holder.appIcon.setImageDrawable(appInfo.icon);
        holder.trackCheckBox.setChecked(appInfo.isTracked);

        holder.itemView.setOnClickListener(v -> {
            appInfo.isTracked = !appInfo.isTracked; 
            holder.trackCheckBox.setChecked(appInfo.isTracked); 
            saveTrackedApps(); 
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    private void saveTrackedApps() {
        Set<String> trackedPackages = SettingsActivity.getTrackedApps(context); 
        for (AppInfo appInfo : appList) {
            if (appInfo.isTracked) {
                trackedPackages.add(appInfo.packageName);
            } else {
                trackedPackages.remove(appInfo.packageName);
            }
        }
        SettingsActivity.saveTrackedApps(context, trackedPackages); 
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        CheckBox trackCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.imageViewAppIcon);
            appName = itemView.findViewById(R.id.textViewAppName);
            trackCheckBox = itemView.findViewById(R.id.checkBoxTrackApp);
        }
    }
} 