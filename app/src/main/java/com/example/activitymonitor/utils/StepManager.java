package com.example.activitymonitor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

public class StepManager {
    private static final String TAG = "StepManager";

    // Ключі для SharedPreferences
    private static final String PREF_NAME = "step_manager_prefs";
    private static final String KEY_OFFSET = "steps_offset";
    private static final String KEY_LAST_SENSOR_STEPS = "last_sensor_steps";
    private static final String KEY_LAST_BOOT_TIME = "last_boot_time";
    private static final String KEY_TOTAL_STEPS = "total_steps_today";
    private static final String KEY_LAST_SAVED_DATE = "last_saved_date";

    private SharedPreferences prefs;
    private Context context;

    public StepManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        initializeStepTracking();
    }

    /**
     * Ініціалізація відстеження кроків
     */
    private void initializeStepTracking() {
        long currentBootTime = getBootTime();
        long lastBootTime = prefs.getLong(KEY_LAST_BOOT_TIME, 0);
        String today = DateUtils.getCurrentDate();
        String lastSavedDate = prefs.getString(KEY_LAST_SAVED_DATE, "");

        Log.d(TAG, "Initialize - Current boot: " + currentBootTime + ", Last boot: " + lastBootTime);

        // Якщо дата змінилася, скидаємо лічильник
        if (!today.equals(lastSavedDate)) {
            Log.d(TAG, "New day detected, resetting steps");
            resetDailySteps();
            prefs.edit().putString(KEY_LAST_SAVED_DATE, today).apply();
        }

        // Якщо система перезавантажилася, оновлюємо offset
        if (currentBootTime != lastBootTime) {
            Log.d(TAG, "System reboot detected, updating boot time");
            prefs.edit().putLong(KEY_LAST_BOOT_TIME, currentBootTime).apply();
        }
    }

    /**
     * Обчислює поточну кількість кроків на основі даних сенсора
     */
    public int calculateCurrentSteps(int sensorSteps) {
        int offset = prefs.getInt(KEY_OFFSET, 0);
        int lastSensorSteps = prefs.getInt(KEY_LAST_SENSOR_STEPS, 0);
        int totalSteps = prefs.getInt(KEY_TOTAL_STEPS, 0);

        Log.d(TAG, "Calculate - Sensor: " + sensorSteps + ", Offset: " + offset +
                ", Last sensor: " + lastSensorSteps + ", Total: " + totalSteps);

        // Якщо це перший запуск або сенсор перезапустився
        if (offset == 0 || sensorSteps < lastSensorSteps) {
            offset = sensorSteps - totalSteps;
            prefs.edit().putInt(KEY_OFFSET, offset).apply();
            Log.d(TAG, "New offset set: " + offset);
        }

        // Обчислюємо поточні кроки
        int currentSteps = sensorSteps - offset;

        // Переконуємося, що кроки не зменшуються
        if (currentSteps < totalSteps) {
            currentSteps = totalSteps;
        }

        // Зберігаємо поточний стан
        saveCurrentState(sensorSteps, currentSteps);

        Log.d(TAG, "Final steps: " + currentSteps);
        return currentSteps;
    }

    /**
     * Зберігає поточний стан для майбутніх обчислень
     */
    private void saveCurrentState(int sensorSteps, int currentSteps) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LAST_SENSOR_STEPS, sensorSteps);
        editor.putInt(KEY_TOTAL_STEPS, currentSteps);
        editor.apply();

        Log.d(TAG, "State saved - Sensor: " + sensorSteps + ", Total: " + currentSteps);
    }

    /**
     * Скидає денний лічильник кроків
     */
    public void resetDailySteps() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TOTAL_STEPS, 0);
        editor.putInt(KEY_OFFSET, 0);
        editor.putInt(KEY_LAST_SENSOR_STEPS, 0);
        editor.apply();

        Log.d(TAG, "Daily steps reset");
    }

    /**
     * Отримує час останнього завантаження системи
     */
    private long getBootTime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    /**
     * Отримує збережену кількість кроків
     */
    public int getSavedSteps() {
        return prefs.getInt(KEY_TOTAL_STEPS, 0);
    }

    /**
     * Очищає всі дані (для тестування)
     */
    public void clearAllData() {
        prefs.edit().clear().apply();
        Log.d(TAG, "All step data cleared");
    }
}