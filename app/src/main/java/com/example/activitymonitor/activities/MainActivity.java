package com.example.activitymonitor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.activitymonitor.R;
import com.example.activitymonitor.fragments.AnalyticsFragment;
import com.example.activitymonitor.fragments.HomeFragment;
import com.example.activitymonitor.fragments.ProfileFragment;
import com.example.activitymonitor.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        Log.d(TAG, "MainActivity created - checking login status");

        // ВАЖЛИВО: перевірка логіну ПЕРШ ніж setContentView
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to LoginActivity");
            redirectToLogin();
            return; // Не продовжуємо створення активності
        }

        Log.d(TAG, "User is logged in, setting up MainActivity");
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBottomNavigation();

        // Завантажуємо домашній фрагмент за замовчуванням
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Додаткова перевірка при поверненні до активності
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User logged out during onResume, redirecting");
            redirectToLogin();
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_analytics) {
                    selectedFragment = new AnalyticsFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                return loadFragment(selectedFragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void redirectToLogin() {
        Log.d(TAG, "Redirecting to LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Важливо: закриваємо MainActivity
    }
}