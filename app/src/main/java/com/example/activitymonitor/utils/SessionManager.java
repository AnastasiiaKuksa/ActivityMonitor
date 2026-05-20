package com.example.activitymonitor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.activitymonitor.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    private static final String PREF_NAME = "ActivityMonitorSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_DATA = "userData";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_WEIGHT = "user_weight";
    private static final String KEY_USER_HEIGHT = "user_height";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    // ОСНОВНІ МЕТОДИ ДЛЯ РОБОТИ З СЕСІЄЮ

    /**
     * Створює сесію користувача при логіні
     */
    public void createLoginSession(User user) {
        String userJson = gson.toJson(user);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_DATA, userJson);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.apply();
    }

    /**
     * Зберігає/оновлює дані користувача
     */
    public void saveUser(User user) {
        if (user != null) {
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER_DATA, userJson);
            editor.putInt(KEY_USER_ID, user.getId());
            editor.apply();
        }
    }

    /**
     * Отримує поточного користувача з сесії
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) return null;
        String userJson = pref.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    /**
     * Перевіряє чи користувач залогінений
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Видаляє сесію (вихід)
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    // ДОДАТКОВІ МЕТОДИ ДЛЯ ЗРУЧНОСТІ

    /**
     * Оновлює дані користувача в сесії
     */
    public void updateUser(User user) {
        if (isLoggedIn() && user != null) {
            saveUser(user);
        }
    }

    /**
     * Отримує ID поточного користувача
     */
    public int getCurrentUserId() {
        int userId = pref.getInt(KEY_USER_ID, -1);
        if (userId != -1) return userId;

        User user = getCurrentUser();
        return user != null ? user.getId() : -1;
    }

    /**
     * Отримує email поточного користувача
     */
    public String getUserEmail() {
        User user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Отримує ім'я поточного користувача
     */
    public String getUserName() {
        User user = getCurrentUser();
        return user != null ? user.getName() : null;
    }

    /**
     * Отримує вік поточного користувача
     */
    public int getUserAge() {
        User user = getCurrentUser();
        return user != null ? user.getAge() : 0;
    }

    /**
     * Отримує вагу поточного користувача
     */
    public double getUserWeight() {
        // Спочатку пробуємо отримати з окремого ключа
        float weight = pref.getFloat(KEY_USER_WEIGHT, -1f);
        if (weight != -1f) return weight;

        // Якщо немає, отримуємо з об'єкта користувача
        User user = getCurrentUser();
        return user != null ? user.getWeight() : 70.0; // стандартна вага
    }

    /**
     * Отримує зріст поточного користувача
     */
    public double getUserHeight() {
        // Спочатку пробуємо отримати з окремого ключа
        float height = pref.getFloat(KEY_USER_HEIGHT, -1f);
        if (height != -1f) return height;

        // Якщо немає, отримуємо з об'єкта користувача
        User user = getCurrentUser();
        return user != null ? user.getHeight() : 175.0; // стандартний зріст
    }

    /**
     * Встановлює вагу користувача
     */
    public void setUserWeight(float weight) {
        editor.putFloat(KEY_USER_WEIGHT, weight);
        editor.apply();

        // Оновлюємо також в об'єкті користувача
        User user = getCurrentUser();
        if (user != null) {
            user.setWeight(weight);
            saveUser(user);
        }
    }

    /**
     * Встановлює зріст користувача
     */
    public void setUserHeight(float height) {
        editor.putFloat(KEY_USER_HEIGHT, height);
        editor.apply();

        // Оновлюємо також в об'єкті користувача
        User user = getCurrentUser();
        if (user != null) {
            user.setHeight(height);
            saveUser(user);
        }
    }

    /**
     * Отримує стать поточного користувача
     */
    public String getUserGender() {
        User user = getCurrentUser();
        return user != null ? user.getGender() : null;
    }

    /**
     * Отримує шлях до фото профілю
     */
    public String getUserPhotoPath() {
        User user = getCurrentUser();
        return user != null ? user.getPhotoPath() : null;
    }

    /**
     * Перевіряє чи є збережені дані користувача
     */
    public boolean hasUserData() {
        return pref.contains(KEY_USER_DATA);
    }

    /**
     * Комплексна перевірка автентифікації
     */
    public boolean isAuthenticated() {
        return isLoggedIn() && hasUserData();
    }

    /**
     * Повністю очищає всі дані користувача
     */
    public void clearAllUserData() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_DATA);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_WEIGHT);
        editor.remove(KEY_USER_HEIGHT);
        editor.apply();
    }

    /**
     * Зберігає окремі поля користувача
     */
    public void updateUserField(String field, Object value) {
        User user = getCurrentUser();
        if (user != null) {
            switch (field) {
                case "name":
                    user.setName((String) value);
                    break;
                case "age":
                    user.setAge((Integer) value);
                    break;
                case "weight":
                    user.setWeight((Double) value);
                    setUserWeight(((Double) value).floatValue()); // Оновлюємо окремо
                    break;
                case "height":
                    user.setHeight((Double) value);
                    setUserHeight(((Double) value).floatValue()); // Оновлюємо окремо
                    break;
                case "gender":
                    user.setGender((String) value);
                    break;
                case "photoPath":
                    user.setPhotoPath((String) value);
                    break;
            }
            updateUser(user);
        }
    }

    /**
     * Додатковий метод для перевірки стану сесії
     */
    public String getSessionStatus() {
        return "LoggedIn: " + isLoggedIn() +
                ", HasUserData: " + hasUserData() +
                ", UserId: " + getCurrentUserId() +
                ", Weight: " + getUserWeight() +
                ", Height: " + getUserHeight();
    }
}