package com.example.dogsitmalta.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dogsitmalta.database.entities.PostEntity;
import com.example.dogsitmalta.database.entities.UserEntity;

import java.io.File;

/** One-time, read-only import from the old SQLiteOpenHelper file.
 * Runtime operations go exclusively through Room. Old file is deliberately retained as backup.
 * Must be called on a background thread. */
public final class LegacyRoomImporter {
    private static final Object LOCK = new Object();
    private LegacyRoomImporter() {}

    public static void importIfNeeded(Context context) {
        Context app = context.getApplicationContext();
        synchronized (LOCK) {
            android.content.SharedPreferences prefs =
                    app.getSharedPreferences("room_migration", Context.MODE_PRIVATE);
            if (prefs.getBoolean("posts_imported_v1", false) &&
                    prefs.getBoolean("users_imported_v1", false)) return;
            File oldFile = app.getDatabasePath("dogsit_blog.db");
            if (!oldFile.exists()) {
                // Fresh installs need no import. Don't create the legacy database.
                prefs.edit().putBoolean("posts_imported_v1", true)
                        .putBoolean("users_imported_v1", true).commit();
                return;
            }
            BlogRoomDatabase room = BlogRoomDatabase.get(app);
            try (SQLiteDatabase old = SQLiteDatabase.openDatabase(
                    oldFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
                final boolean importPosts = !prefs.getBoolean("posts_imported_v1", false);
                final boolean importUsers = !prefs.getBoolean("users_imported_v1", false);
                // A single Room transaction makes repeated attempts safe after interruption.
                room.runInTransaction(() -> {
                    if (importPosts && hasTable(old, "posts")) {
                        // Older schemas (v1/v2) may not contain synchronization columns.
                        String projection = "SELECT id,title,message,location,image_path,created_at,updated_at," +
                                optional(old, "posts", "latitude", "NULL") + "," +
                                optional(old, "posts", "longitude", "NULL") + "," +
                                optional(old, "posts", "remote_id", "NULL") + "," +
                                optional(old, "posts", "owner_uid", "NULL") + "," +
                                optional(old, "posts", "is_dirty", "1") + "," +
                                optional(old, "posts", "is_deleted", "0") + " FROM posts";
                        try (Cursor c = old.rawQuery(projection, null)) {
                            while (c.moveToNext()) {
                                PostEntity row = new PostEntity();
                                row.id = c.getLong(0); row.title = c.getString(1);
                                row.message = c.getString(2); row.location = c.getString(3);
                                row.imagePath = c.getString(4); row.createdAt = c.getLong(5);
                                row.updatedAt = c.getLong(6);
                                row.latitude = c.isNull(7) ? null : c.getDouble(7);
                                row.longitude = c.isNull(8) ? null : c.getDouble(8);
                                row.remoteId = c.getString(9); row.ownerUid = c.getString(10);
                                row.dirty = c.getInt(11) != 0; row.deleted = c.getInt(12) != 0;
                                room.postDao().insert(row);
                            }
                        }
                    }
                    if (importUsers && hasTable(old, "users")) {
                        try (Cursor c = old.rawQuery("SELECT UserID,UserName,UserSurname,UserEmail," +
                                "UserPassword,UserDateCreated,UserDateUpdated,UserIsActive," +
                                "firebase_uid,profile_updated_at FROM users", null)) {
                            while (c.moveToNext()) {
                                UserEntity row = new UserEntity();
                                row.id = c.getLong(0); row.name = c.getString(1);
                                row.surname = c.getString(2); row.email = c.getString(3);
                                row.passwordHash = c.getString(4);
                                row.createdAt = c.getLong(5);
                                row.passwordUpdatedAt = c.isNull(6) ? null : c.getLong(6);
                                row.active = c.getInt(7) != 0;
                                row.firebaseUid = c.getString(8);
                                row.profileUpdatedAt = c.getLong(9);
                                room.userDao().insert(row);
                            }
                        }
                    }
                });
                if (!prefs.edit().putBoolean("posts_imported_v1", true)
                        .putBoolean("users_imported_v1", true).commit()) {
                    throw new IllegalStateException("Could not record Room import completion");
                }
            }
        }
    }
    private static String optional(SQLiteDatabase db, String table, String column, String fallback) {
        // All identifiers are internal constants, never supplied by app users.
        try (Cursor c = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (c.moveToNext()) if (column.equals(c.getString(1))) return column;
        }
        return fallback;
    }
    private static boolean hasTable(SQLiteDatabase db, String name) {
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{name})) { return c.moveToFirst(); }
    }
}
