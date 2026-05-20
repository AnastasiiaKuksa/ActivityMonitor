package com.example.activitymonitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.activitymonitor.models.Goal;
import com.example.activitymonitor.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Методи для відкриття/закриття бази даних
    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // ... ІСНУЮЧІ МЕТОДИ ДЛЯ КОРИСТУВАЧІВ ...

    // === МЕТОДИ ДЛЯ РОБОТИ З ЦІЛЯМИ ===

    /**
     * Додає нову ціль
     */
    public long addGoal(Goal goal) {
        open();
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

        long result = db.insert(DatabaseHelper.TABLE_GOALS, null, values);
        close();
        return result;
    }

    /**
     * Оновлює прогрес цілі
     */
    public boolean updateGoalProgress(int goalId, double currentWeight) {
        // Спочатку отримуємо ціль
        Goal goal = getGoalById(goalId);
        if (goal != null) {
            // Оновлюємо поточну вагу
            goal.setCurrentWeight(currentWeight);
            // Перераховуємо прогрес
            goal.calculateProgress();
            // Оновлюємо в базі даних
            return updateGoal(goal);
        }
        return false;
    }

    /**
     * Оновлює ціль
     */
    public boolean updateGoal(Goal goal) {
        open();
        ContentValues values = new ContentValues();

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

        int result = db.update(DatabaseHelper.TABLE_GOALS, values,
                DatabaseHelper.COLUMN_GOAL_ID + " = ?",
                new String[]{String.valueOf(goal.getId())});

        close();
        return result > 0;
    }

    /**
     * Отримує ціль за ID користувача
     */
    public Goal getGoalByUserId(int userId) {
        open();
        Goal goal = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_GOALS,
                null,
                DatabaseHelper.COLUMN_GOAL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            goal = cursorToGoal(cursor);
            cursor.close();
        }
        close();
        return goal;
    }

    /**
     * Отримує ціль за ID цілі
     */
    public Goal getGoalById(int goalId) {
        open();
        Goal goal = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_GOALS,
                null,
                DatabaseHelper.COLUMN_GOAL_ID + " = ?",
                new String[]{String.valueOf(goalId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            goal = cursorToGoal(cursor);
            cursor.close();
        }
        close();
        return goal;
    }

    /**
     * Видаляє ціль
     */
    public boolean deleteGoal(int goalId) {
        open();
        int result = db.delete(DatabaseHelper.TABLE_GOALS,
                DatabaseHelper.COLUMN_GOAL_ID + " = ?",
                new String[]{String.valueOf(goalId)});
        close();
        return result > 0;
    }

    /**
     * Перевіряє чи є ціль у користувача
     */
    public boolean hasUserGoal(int userId) {
        open();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GOALS,
                new String[]{DatabaseHelper.COLUMN_GOAL_ID},
                DatabaseHelper.COLUMN_GOAL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        boolean hasGoal = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        close();
        return hasGoal;
    }

    /**
     * Отримує всі цілі користувача (якщо потрібно)
     */
    public List<Goal> getUserGoals(int userId) {
        open();
        List<Goal> goals = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_GOALS,
                null,
                DatabaseHelper.COLUMN_GOAL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                goals.add(cursorToGoal(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();
        return goals;
    }

    /**
     * Конвертує Cursor в Goal
     */
    private Goal cursorToGoal(Cursor cursor) {
        Goal goal = new Goal();

        try {
            // Отримуємо індекси колонок
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GOAL_ID);
            int userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GOAL_USER_ID);
            int typeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GOAL_TYPE);
            int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GOAL_TITLE);
            int startWeightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_START_WEIGHT);
            int targetWeightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TARGET_WEIGHT);
            int currentWeightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CURRENT_WEIGHT);
            int startDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_START_DATE);
            int targetDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TARGET_DATE);
            int currentStepIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CURRENT_STEP);
            int currentDescriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CURRENT_DESCRIPTION);
            int progressIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PROGRESS);

            // Встановлюємо значення з перевіркою на наявність колонки
            if (idIndex != -1) goal.setId(cursor.getInt(idIndex));
            if (userIdIndex != -1) goal.setUserId(cursor.getInt(userIdIndex));
            if (typeIndex != -1) goal.setType(cursor.getString(typeIndex));
            if (titleIndex != -1) goal.setTitle(cursor.getString(titleIndex));
            if (startWeightIndex != -1) goal.setStartWeight(cursor.getDouble(startWeightIndex));
            if (targetWeightIndex != -1) goal.setTargetWeight(cursor.getDouble(targetWeightIndex));
            if (currentWeightIndex != -1) goal.setCurrentWeight(cursor.getDouble(currentWeightIndex));
            if (startDateIndex != -1) goal.setStartDate(cursor.getString(startDateIndex));
            if (targetDateIndex != -1) goal.setTargetDate(cursor.getString(targetDateIndex));
            if (currentStepIndex != -1) goal.setCurrentStep(cursor.getString(currentStepIndex));
            if (currentDescriptionIndex != -1) goal.setCurrentDescription(cursor.getString(currentDescriptionIndex));
            if (progressIndex != -1) goal.setProgress(cursor.getFloat(progressIndex));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return goal;
    }

    // ... РЕШТА ІСНУЮЧИХ МЕТОДІВ ДЛЯ КОРИСТУВАЧІВ ...

    // Додавання нового користувача
    public long addUser(User user) {
        open();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COLUMN_NAME, user.getName());
        values.put(DatabaseHelper.COLUMN_AGE, user.getAge());
        values.put(DatabaseHelper.COLUMN_WEIGHT, user.getWeight());
        values.put(DatabaseHelper.COLUMN_HEIGHT, user.getHeight());
        values.put(DatabaseHelper.COLUMN_GENDER, user.getGender());
        values.put(DatabaseHelper.COLUMN_PHOTO_PATH, user.getPhotoPath());

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        close();
        return result;
    }

    // Отримання користувача за email та паролем (для логіну)
    public User getUser(String email, String password) {
        open();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_EMAIL + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?",
                new String[]{email, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        close();
        return user;
    }

    // Отримання користувача за ID
    public User getUserById(int userId) {
        open();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        close();
        return user;
    }

    // Оновлення даних користувача
    public boolean updateUser(User user) {
        open();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_NAME, user.getName());
        values.put(DatabaseHelper.COLUMN_AGE, user.getAge());
        values.put(DatabaseHelper.COLUMN_WEIGHT, user.getWeight());
        values.put(DatabaseHelper.COLUMN_HEIGHT, user.getHeight());
        values.put(DatabaseHelper.COLUMN_GENDER, user.getGender());
        values.put(DatabaseHelper.COLUMN_PHOTO_PATH, user.getPhotoPath());

        int result = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});

        close();
        return result > 0;
    }

    // Перевірка чи існує email
    public boolean isEmailExists(String email) {
        open();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                DatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        close();
        return exists;
    }

    // Перевірка логіну
    public boolean checkUserCredentials(String email, String password) {
        open();
        String[] columns = {DatabaseHelper.COLUMN_USER_ID};
        String selection = DatabaseHelper.COLUMN_EMAIL + " = ? AND " +
                DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }
        close();
        return exists;
    }

    // Отримання користувача за email
    public User getUserByEmail(String email) {
        open();
        User user = null;

        String selection = DatabaseHelper.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        close();
        return user;
    }

    // Отримання всіх користувачів
    public List<User> getAllUsers() {
        open();
        List<User> users = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();
        return users;
    }

    // Видалення користувача
    public boolean deleteUser(int userId) {
        open();
        int result = db.delete(DatabaseHelper.TABLE_USERS,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        close();
        return result > 0;
    }

    // Оновлення пароля
    public boolean updatePassword(int userId, String newPassword) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD, newPassword);

        int result = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        close();
        return result > 0;
    }

    // Конвертація Cursor в User
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)));
        user.setAge(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGE)));
        user.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEIGHT)));
        user.setHeight(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HEIGHT)));
        user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENDER)));
        user.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHOTO_PATH)));

        // Додаємо перевірку на наявність колонки createdAt
        int createdAtIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT);
        if (createdAtIndex != -1) {
            user.setCreatedAt(cursor.getString(createdAtIndex));
        }

        return user;
    }
}