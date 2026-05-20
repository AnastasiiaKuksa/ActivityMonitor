package com.example.activitymonitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.activitymonitor.models.Workout;
import java.util.ArrayList;
import java.util.List;

import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_DATE;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_DURATION;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_INTENSITY;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_NOTES;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_USER_ID;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_WORKOUT_CALORIES;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_WORKOUT_ID;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_WORKOUT_TYPE;
import static com.example.activitymonitor.database.DatabaseHelper.TABLE_WORKOUTS;

public class WorkoutDAO {
    private DatabaseHelper dbHelper;

    public WorkoutDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
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
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Workout> getRecentWorkouts(int userId, int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Workout> workouts = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_WORKOUTS +
                    " WHERE " + COLUMN_USER_ID + " = ?" +
                    " ORDER BY " + COLUMN_DATE + " DESC LIMIT " + limit;

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Workout workout = new Workout();

                    // Безпечне отримання значень
                    int idIndex = cursor.getColumnIndex(COLUMN_WORKOUT_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int typeIndex = cursor.getColumnIndex(COLUMN_WORKOUT_TYPE);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
                    int caloriesIndex = cursor.getColumnIndex(COLUMN_WORKOUT_CALORIES);
                    int intensityIndex = cursor.getColumnIndex(COLUMN_INTENSITY);
                    int notesIndex = cursor.getColumnIndex(COLUMN_NOTES);

                    if (idIndex != -1) workout.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) workout.setUserId(cursor.getInt(userIdIndex));
                    if (typeIndex != -1) workout.setWorkoutType(cursor.getString(typeIndex));
                    if (dateIndex != -1) workout.setDate(cursor.getString(dateIndex));
                    if (durationIndex != -1) workout.setDuration(cursor.getInt(durationIndex));
                    if (caloriesIndex != -1) workout.setCalories(cursor.getInt(caloriesIndex));
                    if (intensityIndex != -1) workout.setIntensity(cursor.getString(intensityIndex));
                    if (notesIndex != -1) workout.setNotes(cursor.getString(notesIndex));

                    workouts.add(workout);
                } while (cursor.moveToNext());
            }
            return workouts;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Workout> getUserWorkouts(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Workout> workouts = new ArrayList<>();
        Cursor cursor = null;

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
                    int typeIndex = cursor.getColumnIndex(COLUMN_WORKOUT_TYPE);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                    int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
                    int caloriesIndex = cursor.getColumnIndex(COLUMN_WORKOUT_CALORIES);
                    int intensityIndex = cursor.getColumnIndex(COLUMN_INTENSITY);
                    int notesIndex = cursor.getColumnIndex(COLUMN_NOTES);

                    if (idIndex != -1) workout.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) workout.setUserId(cursor.getInt(userIdIndex));
                    if (typeIndex != -1) workout.setWorkoutType(cursor.getString(typeIndex));
                    if (dateIndex != -1) workout.setDate(cursor.getString(dateIndex));
                    if (durationIndex != -1) workout.setDuration(cursor.getInt(durationIndex));
                    if (caloriesIndex != -1) workout.setCalories(cursor.getInt(caloriesIndex));
                    if (intensityIndex != -1) workout.setIntensity(cursor.getString(intensityIndex));
                    if (notesIndex != -1) workout.setNotes(cursor.getString(notesIndex));

                    workouts.add(workout);
                } while (cursor.moveToNext());
            }
            return workouts;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}