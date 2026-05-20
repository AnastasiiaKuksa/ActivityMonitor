package com.example.activitymonitor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activitymonitor.R;
import com.example.activitymonitor.database.ActivityDAO;
import com.example.activitymonitor.database.SleepDAO;
import com.example.activitymonitor.database.WorkoutDAO;
import com.example.activitymonitor.models.DailyActivity;
import com.example.activitymonitor.models.SleepRecord;
import com.example.activitymonitor.models.Workout;
import com.example.activitymonitor.utils.ActivityTracker;
import com.example.activitymonitor.utils.DateUtils;
import com.example.activitymonitor.utils.SessionManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private SessionManager sessionManager;
    private ActivityDAO activityDAO;
    private SleepDAO sleepDAO;
    private WorkoutDAO workoutDAO;
    private ActivityTracker activityTracker;

    private TextView tvSteps, tvCalories, tvDistance, tvActiveMinutes;
    private TextView tvSleepDuration, tvSleepQuality, tvSleepEfficiency;
    private ProgressBar pbSteps, pbCalories, pbSleep;
    private PieChart pieChart;
    private Button btnAddSleep, btnAddWorkout;

    private RecyclerView rvRecentSleep;
    private RecyclerView rvRecentWorkouts;

    private SleepAdapter sleepAdapter;
    private WorkoutAdapter workoutAdapter;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        initializeDAOs();
        setupRecyclerViews();
        setupClickListeners();
        startActivityTracking();
        loadTodayData();
        loadRecentData();

        return view;
    }

    private void initializeViews(View view) {
        tvSteps = view.findViewById(R.id.tvSteps);
        tvCalories = view.findViewById(R.id.tvCalories);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvActiveMinutes = view.findViewById(R.id.tvActiveMinutes);

        tvSleepDuration = view.findViewById(R.id.tvSleepDuration);
        tvSleepQuality = view.findViewById(R.id.tvSleepQuality);
        tvSleepEfficiency = view.findViewById(R.id.tvSleepEfficiency);

        pbSteps = view.findViewById(R.id.pbSteps);
        pbCalories = view.findViewById(R.id.pbCalories);
        pbSleep = view.findViewById(R.id.pbSleep);

        btnAddSleep = view.findViewById(R.id.btnAddSleep);
        btnAddWorkout = view.findViewById(R.id.btnAddWorkout);

        rvRecentSleep = view.findViewById(R.id.rvRecentSleep);
        rvRecentWorkouts = view.findViewById(R.id.rvRecentWorkouts);

        setupPieChart();
    }

    private void setupRecyclerViews() {
        if (rvRecentSleep != null) {
            sleepAdapter = new SleepAdapter(new ArrayList<>());
            rvRecentSleep.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecentSleep.setAdapter(sleepAdapter);
        }

        if (rvRecentWorkouts != null) {
            workoutAdapter = new WorkoutAdapter(new ArrayList<>());
            rvRecentWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecentWorkouts.setAdapter(workoutAdapter);
        }
    }

    private void setupPieChart() {
        if (pieChart == null) return;

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        pieChart.setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.white));
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.black));
        pieChart.setEntryLabelTextSize(12f);
    }

    private void initializeDAOs() {
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
            activityDAO = new ActivityDAO(getContext());
            sleepDAO = new SleepDAO(getContext());
            workoutDAO = new WorkoutDAO(getContext());
        }
    }

    private void setupClickListeners() {
        if (btnAddSleep != null) {
            btnAddSleep.setOnClickListener(v -> showSleepInputDialog());
        }

        if (btnAddWorkout != null) {
            btnAddWorkout.setOnClickListener(v -> showWorkoutInputDialog());
        }
    }

    private void startActivityTracking() {
        if (getContext() != null && sessionManager.isLoggedIn()) {
            activityTracker = new ActivityTracker(getContext(), new ActivityTracker.ActivityListener() {
                @Override
                public void onActivityUpdate(int steps, double distance, int calories, int activeMinutes) {
                    updateActivityData(steps, distance, calories, activeMinutes);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Помилка відстеження: " + error, Toast.LENGTH_SHORT).show();
                }
            });
            activityTracker.startTracking();
        }
    }

    private void updateActivityData(int steps, double distance, int calories, int activeMinutes) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) return;

        int userId = sessionManager.getCurrentUserId();
        String today = DateUtils.getCurrentDate();

        DailyActivity todayActivity = getOrCreateTodayActivity(userId, today, steps, distance, calories, activeMinutes);
        updateUIWithActivityData(todayActivity);
    }

    private DailyActivity getOrCreateTodayActivity(int userId, String date, int steps, double distance, int calories, int activeMinutes) {
        DailyActivity activity = activityDAO.getTodayActivity(userId, date);

        if (activity == null) {
            activity = new DailyActivity(userId, steps, distance, calories, activeMinutes);
        } else {
            activity.setSteps(steps);
            activity.setDistance(distance);
            activity.setCaloriesBurned(calories);
            activity.setActiveMinutes(activeMinutes);
        }

        activityDAO.addDailyActivity(activity);
        return activity;
    }

    private void updateUIWithActivityData(final DailyActivity activity) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                updateActivityUI(activity);
                updatePieChart(activity);
            });
        }
    }

    private void loadTodayData() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            showDefaultData();
            return;
        }

        int userId = sessionManager.getCurrentUserId();
        String today = DateUtils.getCurrentDate();

        if (userId != -1) {
            loadActivityData(userId, today);
            loadSleepData(userId, today);
        } else {
            showDefaultData();
        }
    }

    private void loadRecentData() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) return;

        int userId = sessionManager.getCurrentUserId();

        if (sleepAdapter != null) {
            List<SleepRecord> recentSleep = sleepDAO.getRecentSleepRecords(userId, 5);
            sleepAdapter.updateData(recentSleep);
        }

        if (workoutAdapter != null) {
            List<Workout> recentWorkouts = workoutDAO.getRecentWorkouts(userId, 5);
            workoutAdapter.updateData(recentWorkouts);
        }
    }

    private void loadActivityData(int userId, String date) {
        DailyActivity todayActivity = activityDAO.getTodayActivity(userId, date);

        if (todayActivity != null) {
            updateActivityUI(todayActivity);
            updatePieChart(todayActivity);
        } else {
            showDefaultActivityData();
            updatePieChart(new DailyActivity(userId, 0, 0.0, 0, 0));
        }
    }

    private void loadSleepData(int userId, String date) {
        SleepRecord todaySleep = sleepDAO.getSleepRecord(userId, date);

        if (todaySleep != null) {
            updateSleepUI(todaySleep);
        } else {
            showDefaultSleepData();
        }
    }

    private void updateActivityUI(DailyActivity activity) {
        if (activity == null) return;

        int calculatedCalories = calculateCalories(activity.getSteps(), sessionManager.getUserWeight());

        tvSteps.setText(String.format("%,d", activity.getSteps()));
        tvCalories.setText(String.format("%,d", calculatedCalories));
        tvDistance.setText(String.format("%.1f км", activity.getDistance()));
        tvActiveMinutes.setText(String.valueOf(activity.getActiveMinutes()));

        int stepsProgress = calculateProgress(activity.getSteps(), 10000);
        int caloriesProgress = calculateProgress(calculatedCalories, 500);

        pbSteps.setProgress(stepsProgress);
        pbCalories.setProgress(caloriesProgress);
    }

    private int calculateCalories(int steps, double weight) {
        double weightFactor = weight > 0 ? weight / 70.0 : 1.0;
        return (int) (steps * 0.04 * weightFactor);
    }

    private void updateSleepUI(SleepRecord sleep) {
        if (sleep == null) return;

        tvSleepDuration.setText(String.format("%.1f год", sleep.getDuration()));
        tvSleepQuality.setText(getSleepQualityText(sleep.getSleepQuality()));
        tvSleepEfficiency.setText(String.format("%.0f%%", sleep.getSleepEfficiency()));

        int sleepProgress = (int) Math.min(sleep.getSleepEfficiency(), 100);
        pbSleep.setProgress(sleepProgress);
    }

    private String getSleepQualityText(int quality) {
        switch (quality) {
            case 1: return " Погано";
            case 2: return " Задовільно";
            case 3: return " Добре";
            case 4: return " Відмінно";
            default: return "-";
        }
    }

    private int calculateProgress(int current, int target) {
        if (target == 0) return 0;
        return Math.min((current * 100) / target, 100);
    }

    private void showDefaultActivityData() {
        tvSteps.setText("0");
        tvCalories.setText("0");
        tvDistance.setText("0.0 км");
        tvActiveMinutes.setText("0");

        pbSteps.setProgress(0);
        pbCalories.setProgress(0);
    }

    private void showDefaultSleepData() {
        tvSleepDuration.setText("0.0 год");
        tvSleepQuality.setText("-");
        tvSleepEfficiency.setText("0%");
        pbSleep.setProgress(0);
    }

    private void showDefaultData() {
        showDefaultActivityData();
        showDefaultSleepData();
        updatePieChart(new DailyActivity(-1, 0, 0.0, 0, 0));
    }

    private void updatePieChart(DailyActivity activity) {
        if (pieChart == null || activity == null) return;

        List<PieEntry> entries = new ArrayList<>();

        if (activity.getSteps() > 0) {
            entries.add(new PieEntry(activity.getSteps(), "Кроки"));
        }

        int calories = calculateCalories(activity.getSteps(), sessionManager.getUserWeight());
        if (calories > 0) {
            entries.add(new PieEntry(calories, "Калорії"));
        }

        if (activity.getActiveMinutes() > 0) {
            entries.add(new PieEntry(activity.getActiveMinutes(), "Активні хв"));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "Немає даних"));
            pieChart.setCenterText("Немає даних");
        } else {
            pieChart.setCenterText("Сьогодні");
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        int[] colors = {
                ContextCompat.getColor(requireContext(), R.color.progress_steps),
                ContextCompat.getColor(requireContext(), R.color.progress_calories)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);

        pieChart.setData(data);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    // === МЕТОДИ ДЛЯ ДОДАВАННЯ СНУ І ТРЕНУВАНЬ ===

    private void showSleepInputDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(" Додати запис сну");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sleep_input, null);
        builder.setView(dialogView);

        EditText etSleepDuration = dialogView.findViewById(R.id.etSleepDuration);
        Slider sliderSleepQuality = dialogView.findViewById(R.id.sliderSleepQuality);

        // Встановлюємо слухач для слайдера
        final int[] sleepQuality = {3}; // Значення за замовчуванням
        sliderSleepQuality.addOnChangeListener((slider, value, fromUser) -> {
            sleepQuality[0] = (int) value;
        });

        builder.setPositiveButton("💤 Зберегти", (dialog, which) -> {
            String durationStr = etSleepDuration.getText().toString().trim();

            if (durationStr.isEmpty()) {
                showMessage(" Введіть тривалість сну");
                return;
            }

            try {
                double duration = Double.parseDouble(durationStr);
                if (duration <= 0 || duration > 24) {
                    showMessage(" Тривалість має бути від 0.1 до 24 годин");
                    return;
                }

                saveSleepRecord(duration, sleepQuality[0]);
            } catch (NumberFormatException e) {
                showMessage(" Невірний формат числа");
            }
        });

        builder.setNegativeButton("📝 Скасувати", null);
        builder.show();
    }

    private void showWorkoutInputDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(" Додати тренування");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_workout_input, null);
        builder.setView(dialogView);

        // Отримуємо елементи з нового макету
        Spinner spWorkoutType = dialogView.findViewById(R.id.spWorkoutType);
        EditText etDuration = dialogView.findViewById(R.id.etDuration);
        EditText etCalories = dialogView.findViewById(R.id.etCalories);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Налаштовуємо Spinner з типами тренувань
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.workout_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWorkoutType.setAdapter(adapter);

        // Створюємо діалог
        AlertDialog dialog = builder.create();

        // Обробник для кнопки "Зберегти"
        btnSave.setOnClickListener(v -> {
            String type = spWorkoutType.getSelectedItem().toString().trim();
            String durationStr = etDuration.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();

            if (type.isEmpty() || durationStr.isEmpty() || caloriesStr.isEmpty()) {
                showMessage(" Заповніть всі поля");
                return;
            }

            try {
                int duration = Integer.parseInt(durationStr);
                int calories = Integer.parseInt(caloriesStr);

                if (duration <= 0) {
                    showMessage(" Тривалість має бути більше 0");
                    return;
                }

                if (calories <= 0) {
                    showMessage(" Калорії мають бути більше 0");
                    return;
                }

                saveWorkoutRecord(type, duration, calories);
                dialog.dismiss(); // Закриваємо діалог після збереження
            } catch (NumberFormatException e) {
                showMessage(" Невірний формат чисел");
            }
        });

        // Обробник для кнопки "Скасувати"
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss(); // Закриваємо діалог
        });

        dialog.show();
    }

    private void saveSleepRecord(double duration, int quality) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            showMessage(" Будь ласка, увійдіть в систему");
            return;
        }

        int userId = sessionManager.getCurrentUserId();
        String today = DateUtils.getCurrentDate();

        // Перевіряємо, чи існує вже запис сну на сьогодні
        SleepRecord todaySleep = sleepDAO.getSleepRecord(userId, today);

        if (todaySleep == null) {
            // Створюємо новий запис
            SleepRecord newSleep = new SleepRecord(userId, today, duration, quality);
            long result = sleepDAO.addSleepRecord(newSleep);

            if (result != -1) {
                showMessage(" Запис сну збережено!");
                loadSleepData(userId, today);
                loadRecentData();
            } else {
                showMessage(" Помилка збереження запису сну");
            }
        } else {
            // Оновлюємо існуючий запис
            todaySleep.setDuration(duration);
            todaySleep.setSleepQuality(quality);
            sleepDAO.updateSleepRecord(todaySleep);
            showMessage(" Запис сну оновлено!");
            loadSleepData(userId, today);
            loadRecentData();
        }
    }

    private void saveWorkoutRecord(String type, int duration, int calories) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            showMessage(" Будь ласка, увійдіть в систему");
            return;
        }

        int userId = sessionManager.getCurrentUserId();
        String today = DateUtils.getCurrentDate();

        // Створюємо нове тренування
        Workout workout = new Workout(userId, type, today, duration, calories, "Середня", "");
        long result = workoutDAO.addWorkout(workout);

        if (result != -1) {
            showMessage("Тренування збережено!");
            loadRecentData();

            // Оновлюємо активні хвилини
            updateActivityMinutes(duration);
        } else {
            showMessage("Помилка збереження тренування");
        }
    }

    private void updateActivityMinutes(int workoutDuration) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) return;

        int userId = sessionManager.getCurrentUserId();
        String today = DateUtils.getCurrentDate();

        DailyActivity todayActivity = activityDAO.getTodayActivity(userId, today);
        if (todayActivity != null) {
            // Додаємо час тренування до активних хвилин
            int newActiveMinutes = todayActivity.getActiveMinutes() + workoutDuration;
            todayActivity.setActiveMinutes(newActiveMinutes);
            activityDAO.updateDailyActivity(todayActivity);

            // Оновлюємо UI
            updateActivityUI(todayActivity);
        }
    }

    private void showMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activityTracker != null) {
            activityTracker.resumeTracking();
        }
        loadTodayData();
        loadRecentData();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (activityTracker != null) {
            activityTracker.pauseTracking();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activityTracker != null) {
            activityTracker.stopTracking();
        }
    }

    // Адаптер для списку сну
    private static class SleepAdapter extends RecyclerView.Adapter<SleepAdapter.SleepViewHolder> {
        private List<SleepRecord> sleepRecords;

        public SleepAdapter(List<SleepRecord> sleepRecords) {
            this.sleepRecords = sleepRecords;
        }

        public void updateData(List<SleepRecord> newData) {
            this.sleepRecords = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SleepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sleep, parent, false);
            return new SleepViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SleepViewHolder holder, int position) {
            SleepRecord sleep = sleepRecords.get(position);
            holder.bind(sleep);
        }

        @Override
        public int getItemCount() {
            return sleepRecords.size();
        }

        static class SleepViewHolder extends RecyclerView.ViewHolder {
            private TextView tvDate, tvDuration, tvQuality;

            public SleepViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvDuration = itemView.findViewById(R.id.tvDuration);
                tvQuality = itemView.findViewById(R.id.tvQuality);
            }

            public void bind(SleepRecord sleep) {
                tvDate.setText(DateUtils.formatDateForDisplay(sleep.getDate()));
                tvDuration.setText(String.format("%.1f год", sleep.getDuration()));
                tvQuality.setText(getQualityEmoji(sleep.getSleepQuality()));
            }

            private String getQualityEmoji(int quality) {
                switch (quality) {
                    case 1: return "Погано";
                    case 2: return "Нормально";
                    case 3: return "Добре";
                    case 4: return "Чудово";
                    default: return "Нічого";
                }
            }
        }
    }

    // Адаптер для списку тренувань
    private static class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
        private List<Workout> workouts;

        public WorkoutAdapter(List<Workout> workouts) {
            this.workouts = workouts;
        }

        public void updateData(List<Workout> newData) {
            this.workouts = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
            return new WorkoutViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
            Workout workout = workouts.get(position);
            holder.bind(workout);
        }

        @Override
        public int getItemCount() {
            return workouts.size();
        }

        static class WorkoutViewHolder extends RecyclerView.ViewHolder {
            private TextView tvType, tvDate, tvDuration, tvCalories;

            public WorkoutViewHolder(@NonNull View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvType);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvDuration = itemView.findViewById(R.id.tvDuration);
                tvCalories = itemView.findViewById(R.id.tvCalories);
            }

            public void bind(Workout workout) {
                tvType.setText(workout.getWorkoutType());
                tvDate.setText(DateUtils.formatDateForDisplay(workout.getDate()));
                tvDuration.setText(workout.getDuration() + " хв");
                tvCalories.setText(workout.getCalories() + " ккал");
            }
        }
    }
}