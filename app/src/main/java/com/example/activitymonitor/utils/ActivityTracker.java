package com.example.activitymonitor.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.activitymonitor.utils.StepManager;

public class ActivityTracker implements SensorEventListener {
    private static final String TAG = "ActivityTracker";

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private StepManager stepManager;
    private ActivityListener listener;
    private Context context;

    private int previousSteps = 0;
    private boolean isTracking = false;

    public ActivityTracker(Context context, ActivityListener listener) {
        this.context = context;
        this.listener = listener;
        this.stepManager = new StepManager(context);
        initializeSensors();
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    public void startTracking() {
        if (stepSensor == null) {
            Log.e(TAG, "Step counter sensor not available");
            if (listener != null) {
                listener.onError("Step counter not supported on this device");
            }
            return;
        }

        // Відновлюємо попередні кроки
        previousSteps = stepManager.getSavedSteps();
        Log.d(TAG, "Starting tracking with previous steps: " + previousSteps);

        // Реєструємо слухача сенсора
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        isTracking = true;

        Log.d(TAG, "Step tracking started");
    }

    public void stopTracking() {
        if (sensorManager != null && isTracking) {
            sensorManager.unregisterListener(this);
            isTracking = false;
            Log.d(TAG, "Step tracking stopped");
        }
    }

    public void pauseTracking() {
        if (sensorManager != null && isTracking) {
            sensorManager.unregisterListener(this);
            isTracking = false;
            Log.d(TAG, "Step tracking paused");
        }
    }

    public void resumeTracking() {
        if (stepSensor != null && !isTracking) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            isTracking = true;
            Log.d(TAG, "Step tracking resumed");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int sensorSteps = (int) event.values[0];

            // Обчислюємо реальні кроки з урахуванням offset
            int currentSteps = stepManager.calculateCurrentSteps(sensorSteps);

            // Обчислюємо нові кроки з останнього оновлення
            int newSteps = currentSteps - previousSteps;

            if (newSteps > 0) {
                Log.d(TAG, "New steps detected: " + newSteps + ", Total: " + currentSteps);

                // Оновлюємо дані
                updateActivityData(currentSteps);
                previousSteps = currentSteps;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Можна додати обробку змін точності, якщо потрібно
    }

    private void updateActivityData(int steps) {
        // Тут ваш код для оновлення UI та бази даних
        // Наприклад:
        double distance = calculateDistance(steps);
        int calories = calculateCalories(steps);
        int activeMinutes = calculateActiveMinutes(steps);

        if (listener != null) {
            listener.onActivityUpdate(steps, distance, calories, activeMinutes);
        }
    }

    private double calculateDistance(int steps) {
        // Середня довжина кроку ~0.76 метра
        return steps * 0.76 / 1000.0; // км
    }

    private int calculateCalories(int steps) {
        // Приблизно 0.04 калорії на крок
        return (int) (steps * 0.04);
    }

    private int calculateActiveMinutes(int steps) {
        // Припустимо, що 1000 кроків = 10 активних хвилин
        return steps / 100;
    }

    public interface ActivityListener {
        void onActivityUpdate(int steps, double distance, int calories, int activeMinutes);
        void onError(String error);
    }
}