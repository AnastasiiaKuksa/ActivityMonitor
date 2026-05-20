package com.example.activitymonitor.database;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.activitymonitor.utils.SessionManager;

import java.io.File;

public class ExportActivity extends AppCompatActivity {
    private DataExportManager exportManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exportManager = new DataExportManager(this);

        // Експорт даних
        exportData();
    }

    private void exportData() {
        new Thread(() -> {
            SessionManager sessionManager = new SessionManager(this);
            int userId = sessionManager.getCurrentUserId();

            if (userId != -1) {
                String fileName = "user_data_" + userId + "_" +
                        System.currentTimeMillis() + ".json";

                File downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                File exportFile = new File(downloadsDir, fileName);

                boolean success = exportManager.exportUserData(userId, exportFile.getAbsolutePath());

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this,
                                "Дані експортовано: " + exportFile.getAbsolutePath(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Помилка експорту даних",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
}