package com.example.dogsitmalta.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.example.dogsitmalta.R;
import com.example.dogsitmalta.model.BlogPost;

import java.util.Locale;

/** Opens saved points in whichever mapping application handles geo: URIs by default. */
public final class MapIntentUtils {
    private MapIntentUtils() { }

    public static boolean canOpen(BlogPost post) {
        return hasCoordinates(post) || (post != null && post.getLocation() != null
                && !post.getLocation().trim().isEmpty());
    }

    public static boolean hasCoordinates(BlogPost post) {
        if (post == null || post.getLatitude() == null || post.getLongitude() == null) return false;
        double lat = post.getLatitude(), lng = post.getLongitude();
        return !Double.isNaN(lat) && !Double.isInfinite(lat) && lat >= -90 && lat <= 90
                && !Double.isNaN(lng) && !Double.isInfinite(lng) && lng >= -180 && lng <= 180;
    }

    public static String locationLabel(Context context, BlogPost post) {
        if (post != null && post.getLocation() != null && !post.getLocation().trim().isEmpty())
            return post.getLocation().trim();
        if (hasCoordinates(post))
            return String.format(Locale.US, "%.5f, %.5f", post.getLatitude(), post.getLongitude());
        return context.getString(R.string.map_picker_none);
    }

    public static void openSavedLocation(Context context, BlogPost post) {
        if (!canOpen(post)) return;
        String query;
        if (hasCoordinates(post)) {
            // Coordinates pinpoint the post rather than the centre of the locality.
            query = String.format(Locale.US, "%.6f,%.6f", post.getLatitude(), post.getLongitude());
            if (post.getLocation() != null && !post.getLocation().trim().isEmpty())
                query += "(" + post.getLocation().trim() + ")";
        } else {
            // Older posts may only have their locality text without GPS coordinates.
            query = post.getLocation().trim() + ", Malta";
        }
        Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, geoUri));
        } catch (ActivityNotFoundException ex) {
            // A browser is a safe fallback on devices without a geo: handler.
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query="
                    + Uri.encode(query));
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            } catch (ActivityNotFoundException noBrowser) {
                Toast.makeText(context, R.string.map_no_map_app, Toast.LENGTH_LONG).show();
            }
        }
    }
}
