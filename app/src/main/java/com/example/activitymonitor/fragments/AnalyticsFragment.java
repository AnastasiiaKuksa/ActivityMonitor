package com.example.activitymonitor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.activitymonitor.R;
import com.example.activitymonitor.database.ActivityDAO;
import com.example.activitymonitor.database.SleepDAO;
import com.example.activitymonitor.models.DailyActivity;
import com.example.activitymonitor.models.SleepRecord;
import com.example.activitymonitor.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private SessionManager sessionManager;
    private ActivityDAO activityDAO;
    private SleepDAO sleepDAO;

    private BarChart barChartSteps;
    private LineChart lineChartSleep;
    private TextView tvWeeklySteps, tvStepsProgress, tvAvgSleep, tvSleepProgress;
    private TextView tvMaxActivity, tvBestSleep;

    public AnalyticsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        initializeViews(view);
        initializeDAOs();
        loadWeeklyData();

        return view;
    }

    private void initializeViews(View view) {
        barChartSteps = view.findViewById(R.id.barChartSteps);
        lineChartSleep = view.findViewById(R.id.lineChartSleep);

        tvWeeklySteps = view.findViewById(R.id.tvWeeklySteps);
        tvStepsProgress = view.findViewById(R.id.tvStepsProgress);
        tvAvgSleep = view.findViewById(R.id.tvAvgSleep);
        tvSleepProgress = view.findViewById(R.id.tvSleepProgress);
        tvMaxActivity = view.findViewById(R.id.tvMaxActivity);
        tvBestSleep = view.findViewById(R.id.tvBestSleep);

        setupBarChart(barChartSteps);
        setupLineChart(lineChartSleep);
    }

    private void setupBarChart(BarChart chart) {
        if (chart == null) return;

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);

        // Налаштування осі X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд"}));

        // Налаштування осі Y
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(1000f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.getLegend().setEnabled(false);
    }

    private void setupLineChart(LineChart chart) {
        if (chart == null) return;

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        // Налаштування осі X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд"}));

        // Налаштування осі Y
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.getLegend().setEnabled(false);
    }

    private void initializeDAOs() {
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
            activityDAO = new ActivityDAO(getContext());
            sleepDAO = new SleepDAO(getContext());
        }
    }

    private void loadWeeklyData() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            showEmptyState();
            return;
        }

        int userId = sessionManager.getCurrentUserId();

        if (userId != -1) {
            List<DailyActivity> weeklyActivity = activityDAO.getWeeklyActivity(userId);
            List<SleepRecord> weeklySleep = sleepDAO.getWeeklySleepRecords(userId);

            updateStepsChart(weeklyActivity);
            updateSleepChart(weeklySleep);
            updateStatistics(weeklyActivity, weeklySleep);
        } else {
            showEmptyState();
        }
    }

    private void updateStepsChart(List<DailyActivity> activities) {
        if (barChartSteps == null) return;

        if (activities.isEmpty()) {
            barChartSteps.setVisibility(View.GONE);
            tvWeeklySteps.setText("--");
            tvStepsProgress.setText("Немає даних");
            return;
        }

        barChartSteps.setVisibility(View.VISIBLE);
        List<BarEntry> entries = new ArrayList<>();

        // Заповнюємо графік даними за останні 7 днів
        for (int i = 0; i < Math.min(activities.size(), 7); i++) {
            DailyActivity activity = activities.get(i);
            entries.add(new BarEntry(i, activity.getSteps()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Кроки");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.steps_color));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartSteps.setData(data);
        barChartSteps.animateY(1000);
        barChartSteps.invalidate();
    }

    private void updateSleepChart(List<SleepRecord> sleepRecords) {
        if (lineChartSleep == null) return;

        if (sleepRecords.isEmpty()) {
            lineChartSleep.setVisibility(View.GONE);
            tvAvgSleep.setText("--");
            tvSleepProgress.setText("Немає даних");
            return;
        }

        lineChartSleep.setVisibility(View.VISIBLE);
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < Math.min(sleepRecords.size(), 7); i++) {
            SleepRecord sleep = sleepRecords.get(i);
            entries.add(new Entry(i, (float) sleep.getDuration()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Сон");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.sleep_color));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.sleep_color));
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.sleep_color));
        dataSet.setFillAlpha(50);

        LineData data = new LineData(dataSet);
        lineChartSleep.setData(data);
        lineChartSleep.animateY(1000);
        lineChartSleep.invalidate();
    }

    private void updateStatistics(List<DailyActivity> activities, List<SleepRecord> sleepRecords) {
        // Статистика активності
        if (!activities.isEmpty()) {
            int totalSteps = 0;
            int maxSteps = 0;

            for (DailyActivity activity : activities) {
                totalSteps += activity.getSteps();
                if (activity.getSteps() > maxSteps) {
                    maxSteps = activity.getSteps();
                }
            }

            int avgSteps = totalSteps / activities.size();
            tvWeeklySteps.setText(String.format("%,d", avgSteps));
            tvMaxActivity.setText(String.format("%,d кроки", maxSteps));

            // Простий прогрес (можна замінити на реальні розрахунки)
            tvStepsProgress.setText("Стабільна активність");
        } else {
            tvWeeklySteps.setText("--");
            tvMaxActivity.setText("--");
            tvStepsProgress.setText("Немає даних");
        }

        // Статистика сну
        if (!sleepRecords.isEmpty()) {
            double totalSleep = 0;
            double bestSleep = 0;

            for (SleepRecord sleep : sleepRecords) {
                totalSleep += sleep.getDuration();
                if (sleep.getDuration() > bestSleep) {
                    bestSleep = sleep.getDuration();
                }
            }

            double avgSleep = totalSleep / sleepRecords.size();
            tvAvgSleep.setText(String.format("%.1f год", avgSleep));
            tvBestSleep.setText(String.format("%.1f год", bestSleep));
            tvSleepProgress.setText("Здоровий сон");
        } else {
            tvAvgSleep.setText("--");
            tvBestSleep.setText("--");
            tvSleepProgress.setText("Немає даних");
        }
    }

    private void showEmptyState() {
        if (tvWeeklySteps != null) tvWeeklySteps.setText("--");
        if (tvStepsProgress != null) tvStepsProgress.setText("Увійдіть в акаунт");
        if (tvAvgSleep != null) tvAvgSleep.setText("--");
        if (tvSleepProgress != null) tvSleepProgress.setText("Увійдіть в акаунт");
        if (tvMaxActivity != null) tvMaxActivity.setText("--");
        if (tvBestSleep != null) tvBestSleep.setText("--");

        if (barChartSteps != null) barChartSteps.setVisibility(View.GONE);
        if (lineChartSleep != null) lineChartSleep.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWeeklyData();
    }
}