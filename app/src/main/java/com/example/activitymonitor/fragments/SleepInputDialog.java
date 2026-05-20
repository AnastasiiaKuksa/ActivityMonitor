package com.example.activitymonitor.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import com.example.activitymonitor.R;

public class SleepInputDialog extends Dialog {

    public interface SleepInputListener {
        void onSleepInput(double duration, int quality);
    }

    private SleepInputListener listener;
    private EditText etDuration;
    private RatingBar rbQuality;

    public SleepInputDialog(@NonNull Context context, SleepInputListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sleep_input);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etDuration = findViewById(R.id.etDuration);

        // Встановлюємо підказку в рейтинг
        rbQuality.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // Можна додати логіку для оновлення текстового опису
        });
    }

    private void setupClickListeners() {
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> saveSleepData());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveSleepData() {
        String durationStr = etDuration.getText().toString().trim();

        // Валідація введених даних
        if (durationStr.isEmpty()) {
            etDuration.setError("Введіть тривалість сну");
            return;
        }

        try {
            double duration = Double.parseDouble(durationStr);

            // Перевірка коректності значення
            if (duration <= 0) {
                etDuration.setError("Тривалість повинна бути більше 0");
                return;
            }

            if (duration > 24) {
                etDuration.setError("Тривалість не може бути більше 24 годин");
                return;
            }

            int quality = (int) rbQuality.getRating();

            // Перевірка якості сну
            if (quality < 1) {
                quality = 1; // Мінімальне значення
            }

            if (listener != null) {
                listener.onSleepInput(duration, quality);
            }
            dismiss();

        } catch (NumberFormatException e) {
            etDuration.setError("Невірний формат числа");
        }
    }

    // Метод для встановлення значень за замовчуванням
    public void setDefaultValues(double duration, int quality) {
        if (etDuration != null) {
            etDuration.setText(String.valueOf(duration));
        }
        if (rbQuality != null) {
            rbQuality.setRating(quality);
        }
    }
}