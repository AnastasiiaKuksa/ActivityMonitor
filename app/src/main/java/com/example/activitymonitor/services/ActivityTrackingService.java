package com.example.activitymonitor.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import com.example.activitymonitor.database.ActivityDAO;
import com.example.activitymonitor.models.DailyActivity;
import com.example.activitymonitor.utils.DateUtils;
import com.example.activitymonitor.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityTrackingService extends Service implements SensorEventListener {

    private static final String TAG = "ActivityTracker";

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor accelerometer;

    private SessionManager sessionManager;
    private ActivityDAO activityDAO;

    private int stepCount = 0;
    private int todaySteps = 0;
    private long lastUpdate = 0;
    private float lastX, lastY, lastZ;
    private static final int SHAKE_THRESHOLD = 800;

    private boolean isTracking = false;

    @Override
    public void onCreate() {
        super.onCreate();

        sessionManager = new SessionManager(this);
        activityDAO = new ActivityDAO(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        initializeSensors();
        loadTodaySteps();
    }

    private void initializeSensors() {
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (stepCounterSensor != null) {
                Log.d(TAG, "Step counter sensor is available");
            } else {
                Log.d(TAG, "Step counter sensor NOT available");
            }
        }
    }

    private void loadTodaySteps() {
        int userId = sessionManager.getCurrentUserId();
        String today = DateUtils.getCurrentDate();

        if (userId != -1) {
            DailyActivity todayActivity = activityDAO.getTodayActivity(userId, today);
            if (todayActivity != null) {
                todaySteps = todayActivity.getSteps();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTracking();
        return START_STICKY;
    }

    private void startTracking() {
        if (!isTracking) {
            if (stepCounterSensor != null) {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            isTracking = true;
            Log.d(TAG, "Activity tracking started");
        }
    }

    private void stopTracking() {
        if (isTracking) {
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
            isTracking = false;
            Log.d(TAG, "Activity tracking stopped");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleStepCounter(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometer(event);
        }
    }

    private void handleStepCounter(SensorEvent event) {
        int newStepCount = (int) event.values[0];

        if (stepCount == 0) {
            stepCount = newStepCount;
        } else {
            int stepsSinceLastUpdate = newStepCount - stepCount;
            if (stepsSinceLastUpdate > 0) {
                todaySteps += stepsSinceLastUpdate;
                stepCount = newStepCount;

                saveActivityData();
                sendBroadcastUpdate();
            }
        }
    }

    private void handleAccelerometer(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastUpdate) > 100) {
            long timeDiff = currentTime - lastUpdate;
            lastUpdate = currentTime;

            float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDiff * 10000;

            if (speed > SHAKE_THRESHOLD) {
                // Виявлена активність - можна додати логіку для визначення типу активності
                Log.d(TAG, "Activity detected with speed: " + speed);
            }

            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    private void saveActivityData() {
        int userId = sessionManager.getCurrentUserId();
        String today = DateUtils.getCurrentDate();

        if (userId != -1) {
            // Розраховуємо дистанцію та калорії на основі кроків
            double distance = todaySteps * 0.0008; // Приблизно 0.8 метра на крок
            int calories = (int) (todaySteps * 0.04); // Приблизно 0.04 калорії на крок

            DailyActivity activity = new DailyActivity(userId, todaySteps, distance, calories, 0);
            activityDAO.addDailyActivity(activity);

            Log.d(TAG, "Activity saved: " + todaySteps + " steps, " + distance + " km, " + calories + " calories");
        }
    }

    private void sendBroadcastUpdate() {
        Intent intent = new Intent("ACTIVITY_UPDATE");
        intent.putExtra("steps", todaySteps);
        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Не використовується
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
        Log.d(TAG, "Service destroyed");
    }
}