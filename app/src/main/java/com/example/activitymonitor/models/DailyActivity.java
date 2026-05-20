package com.example.activitymonitor.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyActivity {
    private int id;
    private int userId;
    private String date;
    private int steps;
    private double distance;
    private int caloriesBurned;
    private int activeMinutes;

    public DailyActivity() {}

    public DailyActivity(int userId, int steps, double distance, int caloriesBurned, int activeMinutes) {
        this.userId = userId;
        this.steps = steps;
        this.distance = distance;
        this.caloriesBurned = caloriesBurned;
        this.activeMinutes = activeMinutes;
        this.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public int getActiveMinutes() {
        return activeMinutes;
    }

    public void setActiveMinutes(int activeMinutes) {
        this.activeMinutes = activeMinutes;
    }
}