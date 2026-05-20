package com.example.activitymonitor.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.activitymonitor.R;
import com.example.activitymonitor.database.UserDAO;
import com.example.activitymonitor.models.User;
import com.example.activitymonitor.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        Log.d(TAG, "LoginActivity started - isLoggedIn: " + sessionManager.isLoggedIn());

        // Перевірка чи користувач вже залогінений
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User is already logged in, redirecting to MainActivity");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Log.d(TAG, "User is not logged in, showing login form");
        }
        initializeViews();
        initializeDAOs();

        // Перевірка, чи користувач вже залогінений
        if (sessionManager.isLoggedIn()) {
            redirectToMain();
        }

        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void initializeDAOs() {
        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);
    }

    private void setupClickListeners() {
        // Виправлення: expression lambda
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        // Перевірка користувача в базі даних
        User user = userDAO.getUserByEmail(email);
        if (user != null && userDAO.checkUserCredentials(email, password)) {
            handleSuccessfulLogin(user);
        } else {
            handleFailedLogin();
        }
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void handleSuccessfulLogin(User user) {
        // Виправлення: не потрібно створювати нового користувача, використовуємо існуючого з БД
        sessionManager.createLoginSession(user);

        Toast.makeText(this, "Успішний вхід!", Toast.LENGTH_SHORT).show();
        redirectToMain();
    }

    private void handleFailedLogin() {
        Toast.makeText(this, "Невірний email або пароль", Toast.LENGTH_SHORT).show();
    }

    private void redirectToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}