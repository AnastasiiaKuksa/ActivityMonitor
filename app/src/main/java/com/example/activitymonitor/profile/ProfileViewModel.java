package com.example.activitymonitor.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.activitymonitor.database.UserDAO;
import com.example.activitymonitor.database.GoalDAO;
import com.example.activitymonitor.models.User;
import com.example.activitymonitor.models.Goal;
import com.example.activitymonitor.utils.SessionManager;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<User> _userState = new MutableLiveData<>();
    public LiveData<User> userState = _userState;

    private final MutableLiveData<Boolean> _showEditDialog = new MutableLiveData<>(false);
    public LiveData<Boolean> showEditDialog = _showEditDialog;

    private final MutableLiveData<Boolean> _isLoggedOut = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoggedOut = _isLoggedOut;

    private UserDAO userDAO;
    private GoalDAO goalDAO;
    private SessionManager sessionManager;

    // Конструктор з параметрами для dependency injection
    public ProfileViewModel(UserDAO userDAO, GoalDAO goalDAO, SessionManager sessionManager) {
        this.userDAO = userDAO;
        this.goalDAO = goalDAO;
        this.sessionManager = sessionManager;
        loadUserData();
    }

    // Конструктор за замовчуванням для тестів
    public ProfileViewModel() {
        // Використовується тільки для тестування
    }

    public void setDAOs(UserDAO userDAO, GoalDAO goalDAO, SessionManager sessionManager) {
        this.userDAO = userDAO;
        this.goalDAO = goalDAO;
        this.sessionManager = sessionManager;
        loadUserData();
    }

    public void loadUserData() {
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            // Завантаження реальних даних з БД через SessionManager
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null && userDAO != null) {
                // Оновлюємо дані з БД
                User freshUser = userDAO.getUserById(currentUser.getId());
                if (freshUser != null) {
                    // Завантажуємо ціль користувача
                    if (goalDAO != null) {
                        Goal userGoal = goalDAO.getGoalByUserId(freshUser.getId());
                        freshUser.setGoal(userGoal);
                    }
                    _userState.setValue(freshUser);
                    sessionManager.updateUser(freshUser);
                    return;
                }
            }
        }

        // Якщо БД не доступна, використовуємо тестові дані
        User user = new User(1, "user@example.com", "Іван Іваненко", 30, 75.0, 180.0, "Чоловік");
        user.setGoal(createSampleGoal());
        _userState.setValue(user);
    }

    private Goal createSampleGoal() {
        Goal goal = new Goal();
        goal.setType("WEIGHT_LOSS");
        goal.setTitle("Схуднення");
        goal.setStartWeight(80.0);
        goal.setTargetWeight(70.0);
        goal.setCurrentWeight(75.0);
        goal.setCurrentStep("Активне схуднення");
        goal.setCurrentDescription("Втрачено 5 кг з 10 запланованих");
        goal.calculateProgress();
        return goal;
    }

    public void onEditProfileClick() {
        _showEditDialog.setValue(true);
    }

    public void onEditDialogDismiss() {
        _showEditDialog.setValue(false);
    }

    public void updateUserProfile(User updatedUser) {
        if (userDAO != null && sessionManager != null) {
            // Збереження в БД
            boolean success = userDAO.updateUser(updatedUser);
            if (success) {
                // Оновлюємо ціль, якщо змінилася вага
                Goal currentGoal = updatedUser.getGoal();
                if (currentGoal != null && goalDAO != null &&
                        currentGoal.getCurrentWeight() != updatedUser.getWeight()) {
                    currentGoal.setCurrentWeight(updatedUser.getWeight());
                    currentGoal.calculateProgress();
                    goalDAO.updateGoal(currentGoal);
                }

                _userState.setValue(updatedUser);
                sessionManager.updateUser(updatedUser);
            }
        } else {
            // Якщо БД не доступна, просто оновлюємо стан
            _userState.setValue(updatedUser);
        }
    }

    public void createNewGoal(Goal goal) {
        if (goalDAO != null && sessionManager != null) {
            long result = goalDAO.addGoal(goal);
            if (result != -1) {
                User currentUser = _userState.getValue();
                if (currentUser != null) {
                    currentUser.setGoal(goal);
                    _userState.setValue(currentUser);
                    sessionManager.updateUser(currentUser);
                }
            }
        }
    }

    public void updateGoalProgress(double newWeight) {
        User currentUser = _userState.getValue();
        if (currentUser != null && currentUser.getGoal() != null && goalDAO != null && userDAO != null) {
            Goal goal = currentUser.getGoal();
            goal.setCurrentWeight(newWeight);
            goal.calculateProgress();

            boolean success = goalDAO.updateGoal(goal);
            if (success) {
                currentUser.setGoal(goal);
                // Оновлюємо вагу користувача
                currentUser.setWeight(newWeight);
                userDAO.updateUser(currentUser);

                _userState.setValue(currentUser);
                if (sessionManager != null) {
                    sessionManager.updateUser(currentUser);
                }
            }
        }
    }

    public void logout() {
        if (sessionManager != null) {
            // Використовуємо logoutUser() замість clearAllUserData()
            sessionManager.logoutUser();

            // Логування для дебагу
            Log.d("ProfileViewModel", "User logged out successfully");
            Log.d("ProfileViewModel", "Session status after logout: " +
                    (sessionManager.isLoggedIn() ? "Still logged in" : "Logged out"));
        }

        // Очищаємо локальний стан
        _userState.setValue(null);
        _isLoggedOut.setValue(true);
    }
}