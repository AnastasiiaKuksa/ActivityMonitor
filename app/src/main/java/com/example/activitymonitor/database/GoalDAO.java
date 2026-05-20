package com.example.activitymonitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.activitymonitor.models.Goal;

public class GoalDAO {
    private DatabaseHelper dbHelper;

    public GoalDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public Goal getGoalByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Goal goal = null;

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_GOALS,
                null,
                DatabaseHelper.COLUMN_GOAL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            goal = cursorToGoal(cursor);
            cursor.close();
        }

        return goal;
    }

    public long addGoal(Goal goal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_GOAL_USER_ID, goal.getUserId());
        values.put(DatabaseHelper.COLUMN_GOAL_TYPE, goal.getType());
        values.put(DatabaseHelper.COLUMN_GOAL_TITLE, goal.getTitle());
        values.put(DatabaseHelper.COLUMN_START_WEIGHT, goal.getStartWeight());
        values.put(DatabaseHelper.COLUMN_TARGET_WEIGHT, goal.getTargetWeight());
        values.put(DatabaseHelper.COLUMN_CURRENT_WEIGHT, goal.getCurrentWeight());
        values.put(DatabaseHelper.COLUMN_START_DATE, goal.getStartDate());
        values.put(DatabaseHelper.COLUMN_TARGET_DATE, goal.getTargetDate());
        values.put(DatabaseHelper.COLUMN_CURRENT_STEP, goal.getCurrentStep());
        values.put(DatabaseHelper.COLUMN_CURRENT_DESCRIPTION, goal.getCurrentDescription());
        values.put(DatabaseHelper.COLUMN_PROGRESS, goal.getProgress());

        return db.insert(DatabaseHelper.TABLE_GOALS, null, values);
    }

    public boolean updateGoal(Goal goal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CURRENT_WEIGHT, goal.getCurrentWeight());
        values.put(DatabaseHelper.COLUMN_CURRENT_STEP, goal.getCurrentStep());
        values.put(DatabaseHelper.COLUMN_CURRENT_DESCRIPTION, goal.getCurrentDescription());
        values.put(DatabaseHelper.COLUMN_PROGRESS, goal.getProgress());

        int rowsAffected = db.update(
                DatabaseHelper.TABLE_GOALS,
                values,
                DatabaseHelper.COLUMN_GOAL_ID + " = ?",
                new String[]{String.valueOf(goal.getId())}
        );

        return rowsAffected > 0;
    }

    private Goal cursorToGoal(Cursor cursor) {
        Goal goal = new Goal();
        goal.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_ID)));
        goal.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_USER_ID)));
        goal.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_TYPE)));
        goal.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_TITLE)));
        goal.setStartWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_WEIGHT)));
        goal.setTargetWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TARGET_WEIGHT)));
        goal.setCurrentWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENT_WEIGHT)));
        goal.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE)));
        goal.setTargetDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TARGET_DATE)));
        goal.setCurrentStep(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENT_STEP)));
        goal.setCurrentDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENT_DESCRIPTION)));
        goal.setProgress(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROGRESS)));

        return goal;
    }
}