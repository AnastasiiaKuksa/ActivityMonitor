package com.example.activitymonitor.models;

public class Goal {
    private int id;
    private int userId;
    private String type;
    private String title;
    private double startWeight;
    private double targetWeight;
    private double currentWeight;
    private String startDate;
    private String targetDate;
    private String currentStep;
    private String currentDescription;
    private float progress;

    public Goal() {
    }

    public Goal(int userId, String type, String title, double startWeight,
                double targetWeight, double currentWeight, String startDate, String targetDate) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
        this.currentWeight = currentWeight;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.currentStep = "Початок";
        this.currentDescription = "Початок роботи над ціллю";
        calculateProgress();
    }

    public void calculateProgress() {
        if (type.equals("WEIGHT_LOSS")) {
            double totalLoss = startWeight - targetWeight;
            double currentLoss = startWeight - currentWeight;
            if (totalLoss > 0) {
                this.progress = (float) (currentLoss / totalLoss);
            } else {
                this.progress = 0f;
            }
        } else if (type.equals("MUSCLE_GAIN")) {
            double totalGain = targetWeight - startWeight;
            double currentGain = currentWeight - startWeight;
            if (totalGain > 0) {
                this.progress = (float) (currentGain / totalGain);
            } else {
                this.progress = 0f;
            }
        }
        // Обмежуємо прогрес від 0 до 1
        this.progress = Math.max(0, Math.min(1, this.progress));
    }

    // Гетери і сетери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) {
        this.type = type;
        calculateProgress();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getStartWeight() { return startWeight; }
    public void setStartWeight(double startWeight) {
        this.startWeight = startWeight;
        calculateProgress();
    }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
        calculateProgress();
    }

    public double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
        calculateProgress();
    }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public String getCurrentDescription() { return currentDescription; }
    public void setCurrentDescription(String currentDescription) { this.currentDescription = currentDescription; }

    public float getProgress() { return progress; }
    public void setProgress(float progress) { this.progress = progress; }

    public String getTypeDisplayName() {
        switch (type) {
            case "WEIGHT_LOSS": return "Схуднення";
            case "MUSCLE_GAIN": return "Набір маси";
            case "MAINTENANCE": return "Підтримка форми";
            default: return "Не вказано";
        }
    }
}