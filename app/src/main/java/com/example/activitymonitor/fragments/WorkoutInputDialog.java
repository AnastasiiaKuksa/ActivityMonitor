package com.example.activitymonitor.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.activitymonitor.R;

public class WorkoutInputDialog extends Dialog {

    public interface WorkoutInputListener {
        void onWorkoutInput(String type, int duration, int calories);
    }

    private WorkoutInputListener listener;
    private Spinner spWorkoutType;
    private EditText etDuration, etCalories;

    public WorkoutInputDialog(@NonNull Context context, WorkoutInputListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_workout_input);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        spWorkoutType = findViewById(R.id.spWorkoutType);
        etDuration = findViewById(R.id.etDuration);
        etCalories = findViewById(R.id.etCalories);

        // Типи тренувань
        String[] workoutTypes = {
                "Біг", "Ходьба", "Велосипед", "Плавання",
                "Силове тренування", "Йога", "Пілатес", "Аеробіка"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, workoutTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWorkoutType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> saveWorkoutData());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveWorkoutData() {
        String durationStr = etDuration.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();

        if (durationStr.isEmpty()) {
            etDuration.setError("Введіть тривалість");
            return;
        }

        if (caloriesStr.isEmpty()) {
            etCalories.setError("Введіть калорії");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            int calories = Integer.parseInt(caloriesStr);
            String type = spWorkoutType.getSelectedItem().toString();

            if (duration <= 0) {
                etDuration.setError("Тривалість повинна бути більше 0");
                return;
            }

            if (calories <= 0) {
                etCalories.setError("Калорії повинні бути більше 0");
                return;
            }

            if (listener != null) {
                listener.onWorkoutInput(type, duration, calories);
            }
            dismiss();

        } catch (NumberFormatException e) {
            etDuration.setError("Невірний формат числа");
        }
    }
}