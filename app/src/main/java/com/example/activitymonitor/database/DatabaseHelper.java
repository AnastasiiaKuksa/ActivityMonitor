package com.example.activitymonitor.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ActivityMonitorNEW.db";
    private static final int DATABASE_VERSION = 4;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    // Таблиця користувачів
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_PHOTO_PATH = "photo_path";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Таблиця щоденної активності
    public static final String TABLE_DAILY_ACTIVITY = "daily_activity";
    public static final String COLUMN_ACTIVITY_ID = "activity_id";
    // ДОДАЄМО КОНСТАНТИ ДЛЯ ЩОДЕННОЇ АКТИВНОСТІ
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_CALORIES_BURNED = "calories_burned";
    public static final String COLUMN_ACTIVE_MINUTES = "active_minutes";

    // Таблиця тренувань
    public static final String TABLE_WORKOUTS = "workouts";
    public static final String COLUMN_WORKOUT_ID = "workout_id";
    public static final String COLUMN_WORKOUT_TYPE = "workout_type";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_WORKOUT_CALORIES = "workout_calories";
    public static final String COLUMN_INTENSITY = "intensity";
    public static final String COLUMN_NOTES = "notes";

    // Таблиця сну
    public static final String TABLE_SLEEP = "sleep_records";
    public static final String COLUMN_SLEEP_ID = "sleep_id";
    public static final String COLUMN_SLEEP_DATE = "sleep_date";
    public static final String COLUMN_SLEEP_DURATION = "sleep_duration";
    public static final String COLUMN_DEEP_SLEEP = "deep_sleep";
    public static final String COLUMN_LIGHT_SLEEP = "light_sleep";
    public static final String COLUMN_SLEEP_QUALITY = "sleep_quality";
    public static final String COLUMN_SLEEP_GOAL = "sleep_goal";

    // Таблиця цілей
    public static final String TABLE_GOALS = "user_goals";
    public static final String COLUMN_GOAL_ID = "goal_id";
    public static final String COLUMN_GOAL_USER_ID = "user_id";
    public static final String COLUMN_GOAL_TYPE = "goal_type";
    public static final String COLUMN_GOAL_TITLE = "goal_title";
    public static final String COLUMN_START_WEIGHT = "start_weight";
    public static final String COLUMN_TARGET_WEIGHT = "target_weight";
    public static final String COLUMN_CURRENT_WEIGHT = "current_weight";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_TARGET_DATE = "target_date";
    public static final String COLUMN_CURRENT_STEP = "current_step";
    public static final String COLUMN_CURRENT_DESCRIPTION = "current_description";
    public static final String COLUMN_PROGRESS = "progress";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Таблиця користувачів
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_HEIGHT + " REAL,"
                + COLUMN_GENDER + " TEXT,"
                + COLUMN_PHOTO_PATH + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Таблиця щоденної активності
        String CREATE_DAILY_ACTIVITY_TABLE = "CREATE TABLE " + TABLE_DAILY_ACTIVITY + "("
                + COLUMN_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_DATE + " TEXT NOT NULL,"
                + COLUMN_STEPS + " INTEGER DEFAULT 0,"
                + COLUMN_DISTANCE + " REAL DEFAULT 0,"
                + COLUMN_CALORIES_BURNED + " INTEGER DEFAULT 0,"
                + COLUMN_ACTIVE_MINUTES + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_DATE + ")"
                + ")";

        // Таблиця тренувань
        String CREATE_WORKOUTS_TABLE = "CREATE TABLE " + TABLE_WORKOUTS + "("
                + COLUMN_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_WORKOUT_TYPE + " TEXT NOT NULL,"
                + COLUMN_DATE + " TEXT NOT NULL,"
                + COLUMN_DURATION + " INTEGER NOT NULL,"
                + COLUMN_WORKOUT_CALORIES + " INTEGER DEFAULT 0,"
                + COLUMN_INTENSITY + " TEXT,"
                + COLUMN_NOTES + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";

        // Таблиця сну
        String CREATE_SLEEP_TABLE = "CREATE TABLE " + TABLE_SLEEP + "("
                + COLUMN_SLEEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_SLEEP_DATE + " TEXT NOT NULL,"
                + COLUMN_SLEEP_DURATION + " REAL NOT NULL,"
                + COLUMN_DEEP_SLEEP + " REAL DEFAULT 0,"
                + COLUMN_LIGHT_SLEEP + " REAL DEFAULT 0,"
                + COLUMN_SLEEP_QUALITY + " INTEGER DEFAULT 0,"
                + COLUMN_SLEEP_GOAL + " REAL DEFAULT 8.0,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_SLEEP_DATE + ")"
                + ")";

        // Таблиця цілей
        String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                + COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GOAL_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_GOAL_TYPE + " TEXT NOT NULL,"
                + COLUMN_GOAL_TITLE + " TEXT NOT NULL,"
                + COLUMN_START_WEIGHT + " REAL,"
                + COLUMN_TARGET_WEIGHT + " REAL,"
                + COLUMN_CURRENT_WEIGHT + " REAL,"
                + COLUMN_START_DATE + " TEXT,"
                + COLUMN_TARGET_DATE + " TEXT,"
                + COLUMN_CURRENT_STEP + " TEXT,"
                + COLUMN_CURRENT_DESCRIPTION + " TEXT,"
                + COLUMN_PROGRESS + " REAL DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_GOAL_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE"
                + ")";

        // Виконуємо створення таблиць
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_DAILY_ACTIVITY_TABLE);
        db.execSQL(CREATE_WORKOUTS_TABLE);
        db.execSQL(CREATE_SLEEP_TABLE);
        db.execSQL(CREATE_GOALS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Видаляємо старі таблиці та створюємо нові
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLEEP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_ACTIVITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public SQLiteDatabase getReadableDatabaseSafe() {
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting readable database", e);
        }
        return db;
    }

    public SQLiteDatabase getWritableDatabaseSafe() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting writable database", e);
        }
        return db;
    }

    public boolean exportUserData(int userId, String filePath) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            // Логіка експорту...
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Export error", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private JSONObject cursorToJsonObject(Cursor cursor) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        String[] columnNames = cursor.getColumnNames();

        for (String columnName : columnNames) {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                switch (cursor.getType(columnIndex)) {
                    case Cursor.FIELD_TYPE_NULL:
                        jsonObject.put(columnName, JSONObject.NULL);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        jsonObject.put(columnName, cursor.getLong(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        jsonObject.put(columnName, cursor.getDouble(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        jsonObject.put(columnName, cursor.getString(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        jsonObject.put(columnName, cursor.getBlob(columnIndex));
                        break;
                }
            }
        }
        return jsonObject;
    }

    private boolean writeJsonToFile(JSONObject jsonObject, String filePath) {
        FileWriter fileWriter = null;
        try {
            File file = new File(filePath);
            fileWriter = new FileWriter(file);
            fileWriter.write(jsonObject.toString(4));
            return true;
        } catch (Exception e) {
            Log.e("DataExport", "Error writing to file", e);
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Log.e("DataExport", "Error closing file writer", e);
                }
            }
        }
    }
}