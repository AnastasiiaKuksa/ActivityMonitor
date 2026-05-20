package com.example.activitymonitor.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.activitymonitor.R;
import com.example.activitymonitor.activities.LoginActivity;
import com.example.activitymonitor.database.GoalDAO;
import com.example.activitymonitor.database.UserDAO;
import com.example.activitymonitor.models.Goal;
import com.example.activitymonitor.models.User;
import com.example.activitymonitor.profile.ProfileViewModel;
import com.example.activitymonitor.utils.ImageUtils;
import com.example.activitymonitor.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    // Основні елементи
    private ImageView ivProfilePhoto;
    private TextView tvUserName, tvUserEmail, tvBMI, tvBMICategory;
    private TextView tvUserAge, tvUserGender, tvUserHeight, tvUserWeight;
    private EditText etName, etAge, etWeight, etHeight, etGender;
    private Button btnUpdate, btnLogout, btnSetGoal, btnUpdateProgress;
    private ImageButton btnChangePhoto;

    // Картки
    private MaterialCardView cardProfile, cardBMI, cardGoals;

    // Елементи для блоку цілей
    private View goalsSection;
    private TextView tvGoalTitle, tvProgressPercent, tvCurrentStep;
    private TextView tvStartWeight, tvCurrentWeight, tvTargetWeight, tvGoalDescription;
    private ProgressBar progressBar;

    private ProfileViewModel viewModel;
    private String currentPhotoPath;
    private boolean isEditMode = false;

    // DAO та менеджери
    private UserDAO userDAO;
    private GoalDAO goalDAO;
    private SessionManager sessionManager;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ініціалізація DAO та менеджерів
        userDAO = new UserDAO(requireContext());
        goalDAO = new GoalDAO(requireContext());
        sessionManager = new SessionManager(requireContext());

        initializeViews(view);
        setupViewModel();
        return view;
    }

    private void setupViewModel() {
        // Створюємо кастомну фабрику для ViewModel
        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ProfileViewModel(userDAO, goalDAO, sessionManager);
            }
        };

        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);

        // Спостерігаємо за змінами даних користувача - ВИКОРИСТОВУЄМО ПРЯМО ПОЛЕ
        viewModel.userState.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserDisplay(user);
            }
        });

        // Спостерігаємо за станом виходу - ВИКОРИСТОВУЄМО ПРЯМО ПОЛЕ
        viewModel.isLoggedOut.observe(getViewLifecycleOwner(), loggedOut -> {
            if (loggedOut) {
                redirectToLogin();
            }
        });
    }

    private void initializeViews(View view) {
        // Основні елементи
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvBMI = view.findViewById(R.id.tvBMI);
        tvBMICategory = view.findViewById(R.id.tvBMICategory);

        // TextView для відображення даних
        tvUserAge = view.findViewById(R.id.tvUserAge);
        tvUserGender = view.findViewById(R.id.tvUserGender);
        tvUserHeight = view.findViewById(R.id.tvUserHeight);
        tvUserWeight = view.findViewById(R.id.tvUserWeight);

        // EditText для редагування
        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeight);
        etGender = view.findViewById(R.id.etGender);

        // Кнопки
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnSetGoal = view.findViewById(R.id.btnSetGoal);
        btnUpdateProgress = view.findViewById(R.id.btnUpdateProgress);

        // Картки
        cardProfile = view.findViewById(R.id.cardProfile);
        cardBMI = view.findViewById(R.id.cardBMI);
        cardGoals = view.findViewById(R.id.cardGoals);

        // Елементи блоку цілей
        goalsSection = view.findViewById(R.id.goalsSection);
        tvGoalTitle = view.findViewById(R.id.tvGoalTitle);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvCurrentStep = view.findViewById(R.id.tvCurrentStep);
        tvStartWeight = view.findViewById(R.id.tvStartWeight);
        tvCurrentWeight = view.findViewById(R.id.tvCurrentWeight);
        tvTargetWeight = view.findViewById(R.id.tvTargetWeight);
        tvGoalDescription = view.findViewById(R.id.tvGoalDescription);

        setupClickListeners();
        switchToViewMode();
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(v -> {
            if (isEditMode) {
                updateProfile();
            } else {
                switchToEditMode();
            }
        });

        btnChangePhoto.setOnClickListener(v -> selectImage());
        btnLogout.setOnClickListener(v -> logoutUser());
        btnSetGoal.setOnClickListener(v -> showSetGoalDialog());
        btnUpdateProgress.setOnClickListener(v -> showUpdateProgressDialog());
    }

    private void updateUserDisplay(User user) {
        if (user == null) return;

        // Відображаємо дані з ViewModel
        tvUserName.setText(user.getName() != null ? user.getName() : "Ім'я не вказано");
        tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "Email не вказано");
        tvUserAge.setText(user.getAge() + " років");
        tvUserHeight.setText(user.getHeight() + " см");
        tvUserWeight.setText(user.getWeight() + " кг");
        tvUserGender.setText(user.getGender() != null ? user.getGender() : "Не вказано");

        // Заповнюємо поля редагування
        etName.setText(user.getName());
        etAge.setText(String.valueOf(user.getAge()));
        etWeight.setText(String.valueOf(user.getWeight()));
        etHeight.setText(String.valueOf(user.getHeight()));
        etGender.setText(user.getGender() != null ? user.getGender() : "");

        updateBMIDisplay(user);
        loadProfilePhoto(user);
        updateGoalsSection(user);
    }

    private void updateBMIDisplay(User user) {
        if (user == null) return;

        double bmi = user.calculateBMI();
        String bmiCategory = user.getBMICategory();

        tvBMI.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        tvBMICategory.setText(bmiCategory);

        // Змінюємо колір відповідно до категорії BMI
        try {
            int colorRes = getBMIColorRes(bmi);
            int cardColorRes = getBMICardColorRes(bmi);

            tvBMICategory.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            cardBMI.setCardBackgroundColor(ContextCompat.getColor(requireContext(), cardColorRes));
        } catch (Exception e) {
            Log.e(TAG, "Error setting BMI colors: " + e.getMessage());
        }
    }

    private int getBMIColorRes(double bmi) {
        if (bmi < 18.5) return R.color.error;        // Недостатня вага
        else if (bmi < 25) return R.color.success;   // Норма
        else if (bmi < 30) return R.color.warning;   // Надлишкова вага
        else return R.color.error;                   // Ожиріння
    }

    private int getBMICardColorRes(double bmi) {
        if (bmi < 18.5) return R.color.bmi_underweight_light;
        else if (bmi < 25) return R.color.bmi_normal_light;
        else if (bmi < 30) return R.color.bmi_overweight_light;
        else return R.color.bmi_obese_light;
    }

    private void loadProfilePhoto(User user) {
        if (user == null) return;

        if (user.getPhotoPath() != null && !user.getPhotoPath().isEmpty()) {
            ImageUtils.loadImageFromStorage(user.getPhotoPath(), ivProfilePhoto);
        } else {
            ivProfilePhoto.setImageResource(R.drawable.ic_person);
        }
    }

    private void updateGoalsSection(User user) {
        if (user == null) return;

        if (user.getGoal() != null) {
            Goal goal = user.getGoal();

            // Оновлюємо дані цілі
            tvGoalTitle.setText(goal.getTitle());
            progressBar.setProgress((int) (goal.getProgress() * 100));
            tvProgressPercent.setText((int) (goal.getProgress() * 100) + "%");
            tvCurrentStep.setText(goal.getCurrentStep());
            tvStartWeight.setText(String.format("%.1f кг", goal.getStartWeight()));
            tvCurrentWeight.setText(String.format("%.1f кг", goal.getCurrentWeight()));
            tvTargetWeight.setText(String.format("%.1f кг", goal.getTargetWeight()));
            tvGoalDescription.setText(goal.getCurrentDescription());

            // Показуємо блок цілей
            goalsSection.setVisibility(View.VISIBLE);
            btnSetGoal.setVisibility(View.GONE);
            btnUpdateProgress.setVisibility(View.VISIBLE);
        } else {
            // Ховаємо блок цілей, показуємо кнопку встановлення цілі
            goalsSection.setVisibility(View.GONE);
            btnSetGoal.setVisibility(View.VISIBLE);
            btnUpdateProgress.setVisibility(View.GONE);
        }
    }

    private void switchToEditMode() {
        isEditMode = true;
        btnUpdate.setText(" Зберегти");

        // Показуємо поля редагування
        etName.setVisibility(View.VISIBLE);
        etAge.setVisibility(View.VISIBLE);
        etWeight.setVisibility(View.VISIBLE);
        etHeight.setVisibility(View.VISIBLE);
        etGender.setVisibility(View.VISIBLE);

        // Ховаємо TextView
        tvUserAge.setVisibility(View.GONE);
        tvUserGender.setVisibility(View.GONE);
        tvUserHeight.setVisibility(View.GONE);
        tvUserWeight.setVisibility(View.GONE);
    }

    private void switchToViewMode() {
        isEditMode = false;
        btnUpdate.setText("️ Редагувати");

        // Ховаємо поля редагування
        etName.setVisibility(View.GONE);
        etAge.setVisibility(View.GONE);
        etWeight.setVisibility(View.GONE);
        etHeight.setVisibility(View.GONE);
        etGender.setVisibility(View.GONE);

        // Показуємо TextView
        tvUserAge.setVisibility(View.VISIBLE);
        tvUserGender.setVisibility(View.VISIBLE);
        tvUserHeight.setVisibility(View.VISIBLE);
        tvUserWeight.setVisibility(View.VISIBLE);
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String gender = etGender.getText().toString().trim();

        // Валідація даних
        if (name.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            showMessage(" Будь ласка, заповніть обов'язкові поля");
            return;
        }

        try {
            // ВИКОРИСТОВУЄМО ПРЯМО ПОЛЕ
            User currentUser = viewModel.userState.getValue();
            if (currentUser != null) {
                // Оновлюємо поля існуючого користувача
                currentUser.setName(name);
                currentUser.setAge(Integer.parseInt(ageStr));
                currentUser.setWeight(Double.parseDouble(weightStr));
                currentUser.setHeight(Double.parseDouble(heightStr));
                currentUser.setGender(gender.isEmpty() ? "Не вказано" : gender);

                // Оновлюємо фото, якщо було вибрано нове
                if (currentPhotoPath != null) {
                    currentUser.setPhotoPath(currentPhotoPath);
                }

                // Оновлюємо через ViewModel
                viewModel.updateUserProfile(currentUser);
                switchToViewMode();
                showMessage(" Профіль успішно оновлено!");
            }
        } catch (NumberFormatException e) {
            showMessage(" Невірний формат чисел у полях");
        }
    }

    private void showSetGoalDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(" Встановити нову ціль");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_goal, null);
        builder.setView(dialogView);

        EditText etGoalTitle = dialogView.findViewById(R.id.etGoalTitle);
        EditText etStartWeight = dialogView.findViewById(R.id.etStartWeight);
        EditText etTargetWeight = dialogView.findViewById(R.id.etTargetWeight);

        // Встановлюємо початкові значення
        // ВИКОРИСТОВУЄМО ПРЯМО ПОЛЕ
        User currentUser = viewModel.userState.getValue();
        if (currentUser != null) {
            etStartWeight.setText(String.valueOf(currentUser.getWeight()));
        }

        builder.setPositiveButton(" Почати", (dialog, which) -> {
            String title = etGoalTitle.getText().toString().trim();
            String startWeightStr = etStartWeight.getText().toString().trim();
            String targetWeightStr = etTargetWeight.getText().toString().trim();

            if (title.isEmpty() || startWeightStr.isEmpty() || targetWeightStr.isEmpty()) {
                showMessage(" Заповніть обов'язкові поля");
                return;
            }

            try {
                double startWeight = Double.parseDouble(startWeightStr);
                double targetWeight = Double.parseDouble(targetWeightStr);

                Goal goal = new Goal();
                goal.setUserId(currentUser.getId());
                goal.setType("WEIGHT_LOSS");
                goal.setTitle(title);
                goal.setStartWeight(startWeight);
                goal.setTargetWeight(targetWeight);
                goal.setCurrentWeight(currentUser.getWeight());
                goal.setStartDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                goal.setCurrentStep("Початок");
                goal.setCurrentDescription("Розпочато роботу над ціллю!");
                goal.calculateProgress();

                viewModel.createNewGoal(goal);
                showMessage(" Ціль успішно встановлено!");

            } catch (NumberFormatException e) {
                showMessage(" Невірний формат чисел");
            }
        });

        builder.setNegativeButton(" Скасувати", null);
        builder.show();
    }

    private void showUpdateProgressDialog() {
        // ВИКОРИСТОВУЄМО ПРЯМО ПОЛЕ
        User currentUser = viewModel.userState.getValue();
        if (currentUser == null || currentUser.getGoal() == null) return;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("📊 Оновити прогрес");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_progress, null);
        builder.setView(dialogView);

        EditText etCurrentWeight = dialogView.findViewById(R.id.etCurrentWeight);
        TextView tvProgressInfo = dialogView.findViewById(R.id.tvProgressInfo);

        // Встановлюємо поточні значення
        Goal currentGoal = currentUser.getGoal();
        etCurrentWeight.setText(String.valueOf(currentGoal.getCurrentWeight()));

        tvProgressInfo.setText(String.format("Поточна ціль: %.1f кг → %.1f кг\nПоточний прогрес: %.0f%%",
                currentGoal.getStartWeight(),
                currentGoal.getTargetWeight(),
                currentGoal.getProgress() * 100));

        builder.setPositiveButton(" Оновити", (dialog, which) -> {
            String currentWeightStr = etCurrentWeight.getText().toString().trim();

            if (currentWeightStr.isEmpty()) {
                showMessage(" Введіть поточну вагу");
                return;
            }

            try {
                double newWeight = Double.parseDouble(currentWeightStr);
                viewModel.updateGoalProgress(newWeight);
                showMessage(" Прогрес оновлено!");
            } catch (NumberFormatException e) {
                showMessage("Невірний формат числа");
            }
        });

        builder.setNegativeButton("↩ Скасувати", null);
        builder.show();
    }

    private void logoutUser() {
        Log.d(TAG, "Logout button clicked");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(" Вихід")
                .setMessage("Ви впевнені, що хочете вийти з акаунту?")
                .setPositiveButton(" Так", (dialog, which) -> {
                    Log.d(TAG, "User confirmed logout");
                    performLogout();
                })
                .setNegativeButton(" Ні", (dialog, which) -> {
                    Log.d(TAG, "User canceled logout");
                })
                .show();
    }



    private void emergencyLogout() {
        Log.d(TAG, "Attempting emergency logout");

        try {
            // Безпосередньо очищаємо SharedPreferences
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences(
                    "ActivityMonitorSession", android.content.Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Примусовий перехід
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        } catch (Exception e) {
            Log.e(TAG, "Emergency logout failed: " + e.getMessage());
        }
    }

    private void redirectToLogin() {
        Log.d(TAG, "Redirecting to login");

        try {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to login: " + e.getMessage());
        }
    }

    private void performLogout() {
        // Перевіряємо, чи фрагмент ще прикріплений до активності
        if (!isAdded() || getContext() == null) {
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Вихід з акаунту")
                .setMessage("Ви впевнені, що хочете вийти?")
                .setPositiveButton("Так", (dialog, which) -> {
                    // Додаткова перевірка перед виконанням логаута
                    if (isAdded() && getContext() != null) {
                        sessionManager.logoutUser();
                        navigateToLogin();
                    }
                })
                .setNegativeButton("Ні", null)
                .show();
    }

    private void navigateToLogin() {
        // Безпечний перехід до логіну
        if (isAdded() && getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void showMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // ВИКОРИСТОВУЄМО ПРЯМО ПОЛЕ
                User currentUser = viewModel.userState.getValue();
                if (currentUser != null) {
                    currentPhotoPath = ImageUtils.saveImageToInternalStorage(requireContext(), selectedImageUri, currentUser.getId());
                    ImageUtils.loadImageFromStorage(currentPhotoPath, ivProfilePhoto);

                    // Оновлюємо шлях до фото в профілі
                    currentUser.setPhotoPath(currentPhotoPath);
                    viewModel.updateUserProfile(currentUser);

                    showMessage("📸 Фото профілю оновлено!");
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Перезавантажуємо дані при поверненні на фрагмент
        if (viewModel != null) {
            viewModel.loadUserData();
        }
    }
}