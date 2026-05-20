package com.example.activitymonitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.activitymonitor.models.DailyActivity;
import com.example.activitymonitor.models.Workout;

import java.util.ArrayList;
import java.util.List;

import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_ACTIVE_MINUTES;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_ACTIVITY_ID;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_CALORIES_BURNED;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_DATE;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_DISTANCE;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_DURATION;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_INTENSITY;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_NOTES;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_STEPS;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_USER_ID;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_WORKOUT_CALORIES;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_WORKOUT_ID;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_WORKOUT_TYPE;
import static com.example.activitymonitor.database.DatabaseHelper.TABLE_DAILY_ACTIVITY;
import static com.example.activitymonitor.database.DatabaseHelper.TABLE_WORKOUTS;

public class ActivityDAO {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "ActivityDAO";

    public ActivityDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long addDailyActivity(DailyActivity activity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_USER_ID, activity.getUserId());
            values.put(COLUMN_DATE, activity.getDate());
            values.put(COLUMN_STEPS, activity.getSteps());
            values.put(COLUMN_DISTANCE, activity.getDistance());
            values.put(COLUMN_CALORIES_BURNED, activity.getCaloriesBurned());
            values.put(COLUMN_ACTIVE_MINUTES, activity.getActiveMinutes());

            return db.insertWithOnConflict(TABLE_DAILY_ACTIVITY,
                    null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e(TAG, "Помилка додавання щоденної активності", e);
            return -1;
        }
        // НЕ закриваємо базу даних тут
    }

    public int updateDailyActivity(DailyActivity activity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_STEPS, activity.getSteps());
            values.put(COLUMN_DISTANCE, activity.getDistance());
            values.put(COLUMN_CALORIES_BURNED, activity.getCaloriesBurned());
            values.put(COLUMN_ACTIVE_MINUTES, activity.getActiveMinutes());

            return db.update(TABLE_DAILY_ACTIVITY, values,
                    COLUMN_USER_ID + " = ? AND " + COLUMN_DATE + " = ?",
                    new String[]{String.valueOf(activity.getUserId()), activity.getDate()});
        } catch (Exception e) {
            Log.e(TAG, "Помилка оновлення щоденної активності", e);
            return 0;
        }
        // НЕ закриваємо базу даних тут
    }

    public DailyActivity getTodayActivity(int userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        DailyActivity activity = null;

        try {
            String query = "SELECT * FROM " + TABLE_DAILY_ACTIVITY +
                    " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_DATE + " = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date});

            if (cursor != null && cursor.moveToFirst()) {
                activity = new DailyActivity();

                int idIndex = cursor.getColumnIndex(COLUMN_ACTIVITY_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                int stepsIndex = cursor.getColumnIndex(COLUMN_STEPS);
                int distanceIndex = cursor.getColumnIndex(COLUMN_DISTANCE);
                int caloriesIndex = cursor.getColumnIndex(COLUMN_CALORIES_BURNED);
                int activeMinutesIndex = cursor.getColumnIndex(COLUMN_ACTIVE_MINUTES);

                if (idIndex != -1) activity.setId(cursor.getInt(idIndex));
                if (userIdIndex != -1) activity.setUserId(cursor.getInt(userIdIndex));
                if (dateIndex != -1) activity.setDate(cursor.getString(dateIndex));
                if (stepsIndex != -1) activity.setSteps(cursor.getInt(stepsIndex));
                if (distanceIndex != -1) activity.setDistance(cursor.getDouble(distanceIndex));
                if (caloriesIndex != -1) activity.setCaloriesBurned(cursor.getInt(caloriesIndex));
                if (activeMinutesIndex != -1) activity.setActiveMinutes(cursor.getInt(activeMinutesIndex));
            }
            return activity;
        } catch (Exception e) {
            Log.e(TAG, "Помилка отримання щоденної активності", e);
            return null;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            // НЕ закриваємо базу даних тут
        }
    }

    public List<DailyActivity> getWeeklyActivity(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<DailyActivity> activities = new ArrayList<>();

        try {
            String query = "SELECT * FROM " + TABLE_DAILY_ACTIVITY +
                    " WHERE " + COLUMN_USER_ID + " = ?" +
                    " ORDER BY " + COLUMN_DATE + " DESC LIMIT 7";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DailyActivity activity = new DailyActivity();

                    int idIndex = cursor.getColumnIndex(COLUMN_ACTIVITY_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    int stepsIndex = cursor.getColumnIndex(COLUMN_STEPS);
                    int distanceIndex = cursor.getColumnIndex(COLUMN_DISTANCE);
                    int caloriesIndex = cursor.getColumnIndex(COLUMN_CALORIES_BURNED);
                    int activeMinutesIndex = cursor.getColumnIndex(COLUMN_ACTIVE_MINUTES);

                    if (idIndex != -1) activity.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) activity.setUserId(cursor.getInt(userIdIndex));
                    if (dateIndex != -1) activity.setDate(cursor.getString(dateIndex));
                    if (stepsIndex != -1) activity.setSteps(cursor.getInt(stepsIndex));
                    if (distanceIndex != -1) activity.setDistance(cursor.getDouble(distanceIndex));
                    if (caloriesIndex != -1) activity.setCaloriesBurned(cursor.getInt(caloriesIndex));
                    if (activeMinutesIndex != -1) activity.setActiveMinutes(cursor.getInt(activeMinutesIndex));

                    activities.add(activity);
                } while (cursor.moveToNext());
            }
            return activities;
        } catch (Exception e) {
            Log.e(TAG, "Помилка отримання тижневої активності", e);
            return activities;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            // НЕ закриваємо базу даних тут
        }
    }

    public long addWorkout(Workout workout) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_USER_ID, workout.getUserId());
            values.put(COLUMN_WORKOUT_TYPE, workout.getWorkoutType());
            values.put(COLUMN_DATE, workout.getDate());
            values.put(COLUMN_DURATION, workout.getDuration());
            values.put(COLUMN_WORKOUT_CALORIES, workout.getCalories());
            values.put(COLUMN_INTENSITY, workout.getIntensity());
            values.put(COLUMN_NOTES, workout.getNotes());

            return db.insert(TABLE_WORKOUTS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Помилка додавання тренування", e);
            return -1;
        }
        // НЕ закриваємо базу даних тут
    }

    public List<Workout> getRecentWorkouts(int userId, int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<Workout> workouts = new ArrayList<>();

        try {
            String query = "SELECT * FROM " + TABLE_WORKOUTS +
                    " WHERE " + COLUMN_USER_ID + " = ?" +
                    " ORDER BY " + COLUMN_DATE + " DESC, " + COLUMN_WORKOUT_ID + " DESC LIMIT " + limit;

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Workout workout = new Workout();

                    int idIndex = cursor.getColumnIndex(COLUMN_WORKOUT_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int workoutTypeIndex = cursor.getColumnIndex(COLUMN_WORKOUT_TYPE);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
                    int caloriesIndex = cursor.getColumnIndex(COLUMN_WORKOUT_CALORIES);
                    int intensityIndex = cursor.getColumnIndex(COLUMN_INTENSITY);
                    int notesIndex = cursor.getColumnIndex(COLUMN_NOTES);

                    if (idIndex != -1) workout.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) workout.setUserId(cursor.getInt(userIdIndex));
                    if (workoutTypeIndex != -1) workout.setWorkoutType(cursor.getString(workoutTypeIndex));
                    if (dateIndex != -1) workout.setDate(cursor.getString(dateIndex));
                    if (durationIndex != -1) workout.setDuration(cursor.getInt(durationIndex));
                    if (caloriesIndex != -1) workout.setCalories(cursor.getInt(caloriesIndex));
                    if (intensityIndex != -1) workout.setIntensity(cursor.getString(intensityIndex));
                    if (notesIndex != -1) workout.setNotes(cursor.getString(notesIndex));

                    workouts.add(workout);
                } while (cursor.moveToNext());
            }
            return workouts;
        } catch (Exception e) {
            Log.e(TAG, "Помилка отримання останніх тренувань", e);
            return workouts;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            // НЕ закриваємо базу даних тут
        }
    }

    public List<Workout> getUserWorkouts(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<Workout> workouts = new ArrayList<>();

        try {
            cursor = db.query(TABLE_WORKOUTS,
                    null,
                    COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, COLUMN_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Workout workout = new Workout();

                    int idIndex = cursor.getColumnIndex(COLUMN_WORKOUT_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int workoutTypeIndex = cursor.getColumnIndex(COLUMN_WORKOUT_TYPE);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
                    int caloriesIndex = cursor.getColumnIndex(COLUMN_WORKOUT_CALORIES);
                    int intensityIndex = cursor.getColumnIndex(COLUMN_INTENSITY);
                    int notesIndex = cursor.getColumnIndex(COLUMN_NOTES);

                    if (idIndex != -1) workout.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) workout.setUserId(cursor.getInt(userIdIndex));
                    if (workoutTypeIndex != -1) workout.setWorkoutType(cursor.getString(workoutTypeIndex));
                    if (dateIndex != -1) workout.setDate(cursor.getString(dateIndex));
                    if (durationIndex != -1) workout.setDuration(cursor.getInt(durationIndex));
                    if (caloriesIndex != -1) workout.setCalories(cursor.getInt(caloriesIndex));
                    if (intensityIndex != -1) workout.setIntensity(cursor.getString(intensityIndex));
                    if (notesIndex != -1) workout.setNotes(cursor.getString(notesIndex));

                    workouts.add(workout);
                } while (cursor.moveToNext());
            }
            return workouts;
        } catch (Exception e) {
            Log.e(TAG, "Помилка отримання тренувань користувача", e);
            return workouts;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            // НЕ закриваємо базу даних тут
        }
    }

    public List<Workout> getWorkoutsByDate(int userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<Workout> workouts = new ArrayList<>();

        try {
            String query = "SELECT * FROM " + TABLE_WORKOUTS +
                    " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_DATE + " = ?" +
                    " ORDER BY " + COLUMN_WORKOUT_ID + " DESC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Workout workout = new Workout();

                    int idIndex = cursor.getColumnIndex(COLUMN_WORKOUT_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int workoutTypeIndex = cursor.getColumnIndex(COLUMN_WORKOUT_TYPE);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
                    int caloriesIndex = cursor.getColumnIndex(COLUMN_WORKOUT_CALORIES);
                    int intensityIndex = cursor.getColumnIndex(COLUMN_INTENSITY);
                    int notesIndex = cursor.getColumnIndex(COLUMN_NOTES);

                    if (idIndex != -1) workout.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) workout.setUserId(cursor.getInt(userIdIndex));
                    if (workoutTypeIndex != -1) workout.setWorkoutType(cursor.getString(workoutTypeIndex));
                    if (dateIndex != -1) workout.setDate(cursor.getString(dateIndex));
                    if (durationIndex != -1) workout.setDuration(cursor.getInt(durationIndex));
                    if (caloriesIndex != -1) workout.setCalories(cursor.getInt(caloriesIndex));
                    if (intensityIndex != -1) workout.setIntensity(cursor.getString(intensityIndex));
                    if (notesIndex != -1) workout.setNotes(cursor.getString(notesIndex));

                    workouts.add(workout);
                } while (cursor.moveToNext());
            }
            return workouts;
        } catch (Exception e) {
            Log.e(TAG, "Помилка отримання тренувань за датою", e);
            return workouts;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            // НЕ закриваємо базу даних тут
        }
    }

    // Метод для експорту всіх даних активності
    public List<DailyActivity> getAllActivities(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<DailyActivity> activities = new ArrayList<>();

        try {
            String query = "SELECT * FROM " + TABLE_DAILY_ACTIVITY +
                    " WHERE " + COLUMN_USER_ID + " = ?" +
                    " ORDER BY " + COLUMN_DATE + " DESC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DailyActivity activity = new DailyActivity();

                    int idIndex = cursor.getColumnIndex(COLUMN_ACTIVITY_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    int stepsIndex = cursor.getColumnIndex(COLUMN_STEPS);
                    int distanceIndex = cursor.getColumnIndex(COLUMN_DISTANCE);
                    int caloriesIndex = cursor.getColumnIndex(COLUMN_CALORIES_BURNED);
                    int activeMinutesIndex = cursor.getColumnIndex(COLUMN_ACTIVE_MINUTES);

                    if (idIndex != -1) activity.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) activity.setUserId(cursor.getInt(userIdIndex));
                    if (dateIndex != -1) activity.setDate(cursor.getString(dateIndex));
                    if (stepsIndex != -1) activity.setSteps(cursor.getInt(stepsIndex));
                    if (distanceIndex != -1) activity.setDistance(cursor.getDouble(distanceIndex));
                    if (caloriesIndex != -1) activity.setCaloriesBurned(cursor.getInt(caloriesIndex));
                    if (activeMinutesIndex != -1) activity.setActiveMinutes(cursor.getInt(activeMinutesIndex));

                    activities.add(activity);
                } while (cursor.moveToNext());
            }
            return activities;
        } catch (Exception e) {
            Log.e(TAG, "Помилка експорту активності", e);
            return activities;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            // НЕ закриваємо базу даних тут
        }
    }
}