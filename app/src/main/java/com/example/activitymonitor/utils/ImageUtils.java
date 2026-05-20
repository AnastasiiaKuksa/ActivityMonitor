package com.example.activitymonitor.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.example.activitymonitor.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {

    public static String saveImageToInternalStorage(Context context, Uri imageUri, int userId) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Створюємо директорію для зображень
            File directory = new File(context.getFilesDir(), "profile_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Видаляємо старе фото
            deleteImageFromStorage(context, userId);

            // Створюємо файл для нового зображення
            File imageFile = new File(directory, "user_" + userId + ".jpg");

            // Стискаємо та зберігаємо зображення
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadImageFromStorage(String path, ImageView imageView) {
        try {
            if (path != null && !path.isEmpty()) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                } else {
                    // Встановлюємо стандартне зображення
                    imageView.setImageResource(R.drawable.default_profile);
                }
            } else {
                imageView.setImageResource(R.drawable.default_profile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.default_profile);
        }
    }

    public static void deleteImageFromStorage(Context context, int userId) {
        try {
            File directory = new File(context.getFilesDir(), "profile_images");
            File imageFile = new File(directory, "user_" + userId + ".jpg");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int min = Math.min(width, height);
        Bitmap output = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);

        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        android.graphics.Paint paint = new android.graphics.Paint();
        android.graphics.Rect rect = new android.graphics.Rect(0, 0, min, min);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(min / 2f, min / 2f, min / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}