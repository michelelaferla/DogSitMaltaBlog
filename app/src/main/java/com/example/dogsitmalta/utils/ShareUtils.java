package com.example.dogsitmalta.utils;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.dogsitmalta.model.BlogPost;

import java.io.File;

public final class ShareUtils {

    private ShareUtils() {
    }

    public static void shareSinglePost(Context context, BlogPost post, String chooserTitle) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, formatPost(post));

        Uri imageUri = getShareableImageUri(context, post);

        if (imageUri != null) {
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.setClipData(ClipData.newRawUri("DogSit Malta post image", imageUri)
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setType("text/plain");
        }

        context.startActivity(Intent.createChooser(intent, chooserTitle));
    }

    private static String formatPost(BlogPost post) {
        StringBuilder result = new StringBuilder();

        result.append(post.getTitle())
                .append("\n\n")
                .append(post.getMessage());

        if (post.getLocation() != null && !post.getLocation().trim().isEmpty()) {
            result.append("\n\nLocation: ").append(post.getLocation().trim());
        }
        Uri app = Uri.parse("https://play.google.com/store/apps/details?id=com.example.dogsitmalta");
        result.append("\n\nShared from DogSit Malta " + app);

        return result.toString();
    }

    private static Uri getShareableImageUri(
            Context context,
            BlogPost post
    ) {
        String imagePath = post.getImagePath();

        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        File file = new File(imagePath);

        if (!file.exists()) {
            return null;
        }

        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );
    }
}
