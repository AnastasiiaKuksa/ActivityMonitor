package com.example.activitymonitor.models;

public class Workout {
    private int id;
    private int userId;
    private String workoutType;
    private String date;
    private int duration;
    private int calories;
    private String intensity;
    private String notes;

    // Конструктор за замовчуванням
    public Workout() {}

    // Основний конструктор
    public Workout(int userId, String workoutType, String date, int duration, int calories, String intensity, String notes) {
        this.userId = userId;
        this.workoutType = workoutType;
        this.date = date;
        this.duration = duration;
        this.calories = calories;
        this.intensity = intensity;
        this.notes = notes;
    }

    // Спрощений конструктор (без intensity та notes)
    public Workout(int userId, String workoutType, String date, int duration, int calories) {
        this.userId = userId;
        this.workoutType = workoutType;
        this.date = date;
        this.duration = duration;
        this.calories = calories;
        this.intensity = "Середня";
        this.notes = "";
    }

    // Геттери та сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public String getIntensity() { return intensity; }
    public void setIntensity(String intensity) { this.intensity = intensity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", userId=" + userId +
                ", workoutType='" + workoutType + '\'' +
                ", date='" + date + '\'' +
                ", duration=" + duration +
                ", calories=" + calories +
                ", intensity='" + intensity + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}