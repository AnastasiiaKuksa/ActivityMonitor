package com.example.activitymonitor.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataExportManager {
    private Context context;
    private DatabaseHelper dbHelper;

    public DataExportManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public boolean exportUserData(int userId, String filePath) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            if (db == null || !db.isOpen()) {
                Log.e("DataExport", "Database is not available");
                return false;
            }

            JSONObject userData = new JSONObject();

            // Експорт даних користувача
            cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                JSONObject userJson = cursorToJsonObject(cursor);
                userData.put("user", userJson);
                cursor.close();
            }

            // Інші дані...
            userData.put("export_date", System.currentTimeMillis());
            userData.put("app_version", "1.0");

            return writeJsonToFile(userData, filePath);

        } catch (Exception e) {
            Log.e("DataExport", "Error exporting data", e);
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private JSONObject cursorToJsonObject(Cursor cursor) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        String[] columnNames = cursor.getColumnNames();

        for (String columnName : columnNames) {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                switch (cursor.getType(columnIndex)) {
                    case Cursor.FIELD_TYPE_NULL:
                        jsonObject.put(columnName, JSONObject.NULL);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        jsonObject.put(columnName, cursor.getLong(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        jsonObject.put(columnName, cursor.getDouble(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        jsonObject.put(columnName, cursor.getString(columnIndex));
                        break;
                    default:
                        jsonObject.put(columnName, cursor.getString(columnIndex));
                        break;
                }
            }
        }
        return jsonObject;
    }

    private boolean writeJsonToFile(JSONObject jsonObject, String filePath) {
        FileWriter fileWriter = null;
        try {
            File file = new File(filePath);
            // Створюємо директорії, якщо не існують
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            fileWriter = new FileWriter(file);
            fileWriter.write(jsonObject.toString(4));
            fileWriter.flush();
            return true;
        } catch (Exception e) {
            Log.e("DataExport", "Error writing to file", e);
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Log.e("DataExport", "Error closing file writer", e);
                }
            }
        }
    }

    public String getDefaultExportPath() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        String fileName = "activity_monitor_export_" +
                System.currentTimeMillis() + ".json";
        return new File(downloadsDir, fileName).getAbsolutePath();
    }
}