package com.example.activitymonitor.adapters;

public class SleepSession {
    private int duration; // у хвилинах
    private double quality; // шкала 1-10

    public SleepSession(int duration, double quality) {
        this.duration = duration;
        this.quality = quality;
    }

    public int getDuration() {
        return duration;
    }

    public double getQuality() {
        return quality;
    }
}
