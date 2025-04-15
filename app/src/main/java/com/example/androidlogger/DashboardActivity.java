package com.example.androidlogger;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import net.sqlcipher.database.SQLiteDatabase;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    private LineChart lineChart;
    private EncryptedDatabaseHelper dbHelper;

    
    private ExecutorService executorService;
    private Handler mainThreadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        SQLiteDatabase.loadLibs(this);

        
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());

        lineChart = findViewById(R.id.lineChart);
        dbHelper = new EncryptedDatabaseHelper(this);

        setupChart();
        loadChartDataFromDb(); 
    }

    private void setupChart() {
        
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setDrawGridBackground(false);

        
        Description description = new Description();
        description.setText("Usage & Unlocks (Last 7 Days)");
        description.setTextSize(12f);
        lineChart.setDescription(description);

        
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); 
        xAxis.setValueFormatter(new DayAxisValueFormatter()); 
        xAxis.setLabelCount(7, true); 

        
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f); 
        leftAxis.setTextColor(Color.BLUE);
        

        
        
        lineChart.getAxisRight().setEnabled(false);

        
        lineChart.getLegend().setEnabled(true);
    }

    private void loadChartDataFromDb() {
        
        lineChart.clear();
        lineChart.setNoDataText("Loading data...");
        lineChart.invalidate();

        executorService.submit(() -> {
            
            int daysToLoad = 7;
            List<Entry> usageEntries = new ArrayList<>();
            List<Entry> unlockEntries = new ArrayList<>();

            try {
                for (int i = 0; i < daysToLoad; i++) {
                    Calendar dayCal = Calendar.getInstance();
                    dayCal.add(Calendar.DAY_OF_YEAR, -i);
                    int year = dayCal.get(Calendar.YEAR);
                    int month = dayCal.get(Calendar.MONTH) + 1; 
                    int day = dayCal.get(Calendar.DAY_OF_MONTH);

                    
                    long usageMillis = dbHelper.getTotalUsageTimeForDay(year, month, day);
                    int unlockCount = dbHelper.getScreenUnlocksForDay(year, month, day);
                    Log.d(TAG, String.format("Queried DB (BG): Date=%d-%02d-%02d, Usage=%d ms, Unlocks=%d", year, month, day, usageMillis, unlockCount));

                    float usageMinutes = TimeUnit.MILLISECONDS.toMinutes(usageMillis);
                    float xValue = (float) (daysToLoad - 1 - i);
                    usageEntries.add(new Entry(xValue, usageMinutes));
                    unlockEntries.add(new Entry(xValue, (float) unlockCount));
                }

                
                boolean allZero = true;
                if (!usageEntries.isEmpty() || !unlockEntries.isEmpty()) {
                    for (Entry entry : usageEntries) {
                        if (entry.getY() != 0f) {
                            allZero = false;
                            break;
                        }
                    }
                    if (allZero) { 
                        for (Entry entry : unlockEntries) {
                            if (entry.getY() != 0f) {
                                allZero = false;
                                break;
                            }
                        }
                    }
                } else {
                    allZero = false; 
                }

                final boolean finalAllZero = allZero; 

                
                mainThreadHandler.post(() -> {
                    
                    updateChart(usageEntries, unlockEntries, daysToLoad, finalAllZero);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading chart data in background", e);
                mainThreadHandler.post(() -> {
                    lineChart.clear();
                    lineChart.setNoDataText("Error loading data.");
                    lineChart.invalidate();
                });
            }
        });
    }

    
    private void updateChart(List<Entry> usageEntries, List<Entry> unlockEntries, int daysLoaded, boolean allEntriesAreZero) {
        
        if (allEntriesAreZero) {
            Log.w(TAG, "All data points are zero for the last " + daysLoaded + " days. Displaying message instead of chart.");
            lineChart.clear();
            lineChart.setNoDataText("No activity recorded yet.");
            lineChart.invalidate();
            return;
        }

        if (usageEntries.isEmpty() && unlockEntries.isEmpty()) {
            Log.w(TAG, "No data found in database for the last " + daysLoaded + " days.");
            lineChart.clear(); 
            lineChart.setNoDataText("No usage data recorded yet.");
            lineChart.invalidate();
            return;
        }

        
        Collections.sort(usageEntries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        

        
        LineDataSet usageDataSet = new LineDataSet(usageEntries, "Usage Time (min)");
        usageDataSet.setColor(Color.BLUE);
        usageDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        usageDataSet.setLineWidth(2f);
        usageDataSet.setCircleColor(Color.BLUE);
        usageDataSet.setCircleRadius(3f);
        usageDataSet.setDrawValues(false);

        LineDataSet unlockDataSet = new LineDataSet(unlockEntries, "Unlocks");
        unlockDataSet.setColor(Color.RED);
        unlockDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        unlockDataSet.setLineWidth(2f);
        unlockDataSet.setCircleColor(Color.RED);
        unlockDataSet.setCircleRadius(3f);
        unlockDataSet.setDrawValues(false);

        
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(usageDataSet);
        
        LineData lineData = new LineData(dataSets);

        
        lineData.setDrawValues(false);

        lineChart.setData(lineData);
        lineChart.invalidate(); 
        Log.d(TAG, "Chart updated on main thread.");
    }

    
    private static class DayAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            int daysAgo = (int) value;
            if (daysAgo == 0) return "Today";
            if (daysAgo == 1) return "Yesterday";
            return daysAgo + "d ago";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 