package com.example.activitymonitor.models;

import com.example.activitymonitor.adapters.SleepSession;

import java.util.ArrayList;
import java.util.List;

public class SleepRecord {
    private int id;
    private int userId;
    private String date;
    private double duration;
    private double deepSleep;
    private double lightSleep;
    private int sleepQuality;
    private double sleepGoal;
    private List<SleepSession> sleepSessions;

    // Конструктор за замовчуванням
    public SleepRecord() {
        this.sleepSessions = new ArrayList<>();
    }

    // Основний конструктор
    public SleepRecord(int userId, String date, double duration, double deepSleep, double lightSleep, int sleepQuality, double sleepGoal) {
        this();
        this.userId = userId;
        this.date = date;
        this.duration = duration;
        this.deepSleep = deepSleep;
        this.lightSleep = lightSleep;
        this.sleepQuality = sleepQuality;
        this.sleepGoal = sleepGoal;
    }

    // Конструктор для спрощеного створення
    public SleepRecord(int userId, String date, double duration, int sleepQuality) {
        this();
        this.userId = userId;
        this.date = date;
        this.duration = duration;
        this.sleepQuality = sleepQuality;
        this.deepSleep = duration * 0.3; // 30% глибокого сну
        this.lightSleep = duration * 0.7; // 70% легкого сну
        this.sleepGoal = 8.0; // Стандартна ціль
    }

    // Геттери та сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }

    public double getDeepSleep() { return deepSleep; }
    public void setDeepSleep(double deepSleep) { this.deepSleep = deepSleep; }

    public double getLightSleep() { return lightSleep; }
    public void setLightSleep(double lightSleep) { this.lightSleep = lightSleep; }

    public int getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(int sleepQuality) { this.sleepQuality = sleepQuality; }

    public double getSleepGoal() { return sleepGoal; }
    public void setSleepGoal(double sleepGoal) { this.sleepGoal = sleepGoal; }

    public List<SleepSession> getSleepSessions() {
        return new ArrayList<>(sleepSessions);
    }

    public void setSleepSessions(List<SleepSession> sleepSessions) {
        this.sleepSessions = new ArrayList<>(sleepSessions);
    }

    // Методи для роботи з сесіями
    public void addSleepSession(SleepSession session) {
        if (sleepSessions == null) {
            sleepSessions = new ArrayList<>();
        }
        sleepSessions.add(session);
    }

    public void removeSleepSession(SleepSession session) {
        if (sleepSessions != null) {
            sleepSessions.remove(session);
        }
    }

    // Розрахункові методи для адаптера
    public double getTotalDuration() {
        if (sleepSessions != null && !sleepSessions.isEmpty()) {
            return sleepSessions.stream()
                    .mapToDouble(SleepSession::getDuration)
                    .sum();
        }
        return duration; // Повертаємо основну тривалість, якщо немає сесій
    }

    public double getAverageQuality() {
        if (sleepSessions != null && !sleepSessions.isEmpty()) {
            return sleepSessions.stream()
                    .mapToDouble(SleepSession::getQuality)
                    .average()
                    .orElse(0.0);
        }
        return sleepQuality; // Повертаємо основну якість, якщо немає сесій
    }

    // Додаткові методи
    public double getSleepEfficiency() {
        return Math.min((duration / sleepGoal) * 100, 100);
    }

    public String getSleepQualityText() {
        int quality = (int) Math.round(getAverageQuality());
        switch (quality) {
            case 1: return "Погано";
            case 2: return "Задовільно";
            case 3: return "Добре";
            case 4: return "Відмінно";
            default: return "Не вказано";
        }
    }

    @Override
    public String toString() {
        return "SleepRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", date='" + date + '\'' +
                ", duration=" + duration +
                ", deepSleep=" + deepSleep +
                ", lightSleep=" + lightSleep +
                ", sleepQuality=" + sleepQuality +
                ", sleepGoal=" + sleepGoal +
                ", sleepSessions=" + (sleepSessions != null ? sleepSessions.size() : 0) +
                '}';
    }
}