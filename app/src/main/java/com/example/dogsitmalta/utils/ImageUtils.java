package com.example.dogsitmalta.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtils {

    private ImageUtils() {
    }

    public static File getImageDirectory(Context context) {
        File directory = new File(context.getFilesDir(), "images");

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Could not create image directory.");
        }

        return directory;
    }

    public static File createImageFile(Context context) throws IOException {
        return File.createTempFile(
                "post_" + System.currentTimeMillis() + "_",
                ".jpg",
                getImageDirectory(context)
        );
    }

    public static String copyUriToInternalStorage(Context context, Uri sourceUri)
            throws IOException {

        File destination = createImageFile(context);

        try (InputStream input =
                     context.getContentResolver().openInputStream(sourceUri);
             FileOutputStream output =
                     new FileOutputStream(destination)) {

            if (input == null) {
                throw new IOException("Unable to open the selected image.");
            }

            byte[] buffer = new byte[8192];
            int length;

            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        }

        return destination.getAbsolutePath();
    }

    public static void deleteInternalImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        File image = new File(imagePath);

        if (image.exists()) {
            //noinspection ResultOfMethodCallIgnored
            image.delete();
        }
    }
}
