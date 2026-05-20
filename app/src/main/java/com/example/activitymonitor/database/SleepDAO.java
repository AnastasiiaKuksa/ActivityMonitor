package com.example.activitymonitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.activitymonitor.models.SleepRecord;
import java.util.ArrayList;
import java.util.List;

import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_DEEP_SLEEP;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_LIGHT_SLEEP;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_SLEEP_DATE;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_SLEEP_DURATION;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_SLEEP_GOAL;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_SLEEP_ID;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_SLEEP_QUALITY;
import static com.example.activitymonitor.database.DatabaseHelper.COLUMN_USER_ID;
import static com.example.activitymonitor.database.DatabaseHelper.TABLE_SLEEP;

public class SleepDAO {
    private DatabaseHelper dbHelper;

    public SleepDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addSleepRecord(SleepRecord sleep) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_USER_ID, sleep.getUserId());
            values.put(COLUMN_SLEEP_DATE, sleep.getDate());
            values.put(COLUMN_SLEEP_DURATION, sleep.getDuration());
            values.put(COLUMN_DEEP_SLEEP, sleep.getDeepSleep());
            values.put(COLUMN_LIGHT_SLEEP, sleep.getLightSleep());
            values.put(COLUMN_SLEEP_QUALITY, sleep.getSleepQuality());
            values.put(COLUMN_SLEEP_GOAL, sleep.getSleepGoal());

            return db.insertWithOnConflict(TABLE_SLEEP, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // ВИПРАВЛЕНИЙ МЕТОД - додав реальну реалізацію
    public int updateSleepRecord(SleepRecord sleep) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_SLEEP_DURATION, sleep.getDuration());
            values.put(COLUMN_DEEP_SLEEP, sleep.getDeepSleep());
            values.put(COLUMN_LIGHT_SLEEP, sleep.getLightSleep());
            values.put(COLUMN_SLEEP_QUALITY, sleep.getSleepQuality());
            values.put(COLUMN_SLEEP_GOAL, sleep.getSleepGoal());

            return db.update(TABLE_SLEEP, values,
                    COLUMN_USER_ID + " = ? AND " + COLUMN_SLEEP_DATE + " = ?",
                    new String[]{String.valueOf(sleep.getUserId()), sleep.getDate()});
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public SleepRecord getSleepRecord(int userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SleepRecord sleep = null;
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_SLEEP,
                    null,
                    COLUMN_USER_ID + " = ? AND " + COLUMN_SLEEP_DATE + " = ?",
                    new String[]{String.valueOf(userId), date},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                sleep = new SleepRecord();

                // Безпечне отримання значень
                int idIndex = cursor.getColumnIndex(COLUMN_SLEEP_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                int dateIndex = cursor.getColumnIndex(COLUMN_SLEEP_DATE);
                int durationIndex = cursor.getColumnIndex(COLUMN_SLEEP_DURATION);
                int deepSleepIndex = cursor.getColumnIndex(COLUMN_DEEP_SLEEP);
                int lightSleepIndex = cursor.getColumnIndex(COLUMN_LIGHT_SLEEP);
                int qualityIndex = cursor.getColumnIndex(COLUMN_SLEEP_QUALITY);
                int goalIndex = cursor.getColumnIndex(COLUMN_SLEEP_GOAL);

                if (idIndex != -1) sleep.setId(cursor.getInt(idIndex));
                if (userIdIndex != -1) sleep.setUserId(cursor.getInt(userIdIndex));
                if (dateIndex != -1) sleep.setDate(cursor.getString(dateIndex));
                if (durationIndex != -1) sleep.setDuration(cursor.getDouble(durationIndex));
                if (deepSleepIndex != -1) sleep.setDeepSleep(cursor.getDouble(deepSleepIndex));
                if (lightSleepIndex != -1) sleep.setLightSleep(cursor.getDouble(lightSleepIndex));
                if (qualityIndex != -1) sleep.setSleepQuality(cursor.getInt(qualityIndex));
                if (goalIndex != -1) sleep.setSleepGoal(cursor.getDouble(goalIndex));
            }
            return sleep;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Інші методи залишаються без змін...
    public List<SleepRecord> getWeeklySleepRecords(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SleepRecord> sleepRecords = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_SLEEP +
                    " WHERE " + COLUMN_USER_ID + " = ?" +
                    " ORDER BY " + COLUMN_SLEEP_DATE + " DESC LIMIT 7";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SleepRecord sleep = new SleepRecord();

                    int idIndex = cursor.getColumnIndex(COLUMN_SLEEP_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int dateIndex = cursor.getColumnIndex(COLUMN_SLEEP_DATE);
                    int durationIndex = cursor.getColumnIndex(COLUMN_SLEEP_DURATION);
                    int deepSleepIndex = cursor.getColumnIndex(COLUMN_DEEP_SLEEP);
                    int lightSleepIndex = cursor.getColumnIndex(COLUMN_LIGHT_SLEEP);
                    int qualityIndex = cursor.getColumnIndex(COLUMN_SLEEP_QUALITY);
                    int goalIndex = cursor.getColumnIndex(COLUMN_SLEEP_GOAL);

                    if (idIndex != -1) sleep.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) sleep.setUserId(cursor.getInt(userIdIndex));
                    if (dateIndex != -1) sleep.setDate(cursor.getString(dateIndex));
                    if (durationIndex != -1) sleep.setDuration(cursor.getDouble(durationIndex));
                    if (deepSleepIndex != -1) sleep.setDeepSleep(cursor.getDouble(deepSleepIndex));
                    if (lightSleepIndex != -1) sleep.setLightSleep(cursor.getDouble(lightSleepIndex));
                    if (qualityIndex != -1) sleep.setSleepQuality(cursor.getInt(qualityIndex));
                    if (goalIndex != -1) sleep.setSleepGoal(cursor.getDouble(goalIndex));

                    sleepRecords.add(sleep);
                } while (cursor.moveToNext());
            }
            return sleepRecords;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<SleepRecord> getRecentSleepRecords(int userId, int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SleepRecord> sleepRecords = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_SLEEP +
                    " WHERE " + COLUMN_USER_ID + " = ?" +
                    " ORDER BY " + COLUMN_SLEEP_DATE + " DESC LIMIT " + limit;

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SleepRecord sleep = new SleepRecord();

                    int idIndex = cursor.getColumnIndex(COLUMN_SLEEP_ID);
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int dateIndex = cursor.getColumnIndex(COLUMN_SLEEP_DATE);
                    int durationIndex = cursor.getColumnIndex(COLUMN_SLEEP_DURATION);
                    int deepSleepIndex = cursor.getColumnIndex(COLUMN_DEEP_SLEEP);
                    int lightSleepIndex = cursor.getColumnIndex(COLUMN_LIGHT_SLEEP);
                    int qualityIndex = cursor.getColumnIndex(COLUMN_SLEEP_QUALITY);
                    int goalIndex = cursor.getColumnIndex(COLUMN_SLEEP_GOAL);

                    if (idIndex != -1) sleep.setId(cursor.getInt(idIndex));
                    if (userIdIndex != -1) sleep.setUserId(cursor.getInt(userIdIndex));
                    if (dateIndex != -1) sleep.setDate(cursor.getString(dateIndex));
                    if (durationIndex != -1) sleep.setDuration(cursor.getDouble(durationIndex));
                    if (deepSleepIndex != -1) sleep.setDeepSleep(cursor.getDouble(deepSleepIndex));
                    if (lightSleepIndex != -1) sleep.setLightSleep(cursor.getDouble(lightSleepIndex));
                    if (qualityIndex != -1) sleep.setSleepQuality(cursor.getInt(qualityIndex));
                    if (goalIndex != -1) sleep.setSleepGoal(cursor.getDouble(goalIndex));

                    sleepRecords.add(sleep);
                } while (cursor.moveToNext());
            }
            return sleepRecords;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public double getAverageSleepDuration(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double average = 0;
        Cursor cursor = null;

        try {
            String query = "SELECT AVG(" + COLUMN_SLEEP_DURATION + ") FROM " +
                    TABLE_SLEEP + " WHERE " + COLUMN_USER_ID + " = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                average = cursor.getDouble(0);
            }
            return average;
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