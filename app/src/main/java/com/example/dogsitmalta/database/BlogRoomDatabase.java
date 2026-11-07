package com.example.dogsitmalta.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.dogsitmalta.database.entities.PostEntity;
import com.example.dogsitmalta.database.entities.UserEntity;
import com.example.dogsitmalta.database.interfaces.PostDao;
import com.example.dogsitmalta.database.interfaces.UserDao;

/** Single Room database for posts and local user profiles. */
@Database(entities = {PostEntity.class, UserEntity.class}, version = 2, exportSchema = false)
public abstract class BlogRoomDatabase extends RoomDatabase {
    private static volatile BlogRoomDatabase instance;
    /** Existing Room v1 installs had only posts. Add the user table without deleting posts. */
    private static final androidx.room.migration.Migration MIGRATION_1_2 =
            new androidx.room.migration.Migration(1, 2) {
        @Override public void migrate(androidx.sqlite.db.SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`UserID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`UserName` TEXT NOT NULL, `UserSurname` TEXT NOT NULL, " +
                    "`UserEmail` TEXT COLLATE NOCASE NOT NULL, `UserPassword` TEXT, " +
                    "`UserDateCreated` INTEGER NOT NULL, `UserDateUpdated` INTEGER, " +
                    "`UserIsActive` INTEGER NOT NULL, `firebase_uid` TEXT, " +
                    "`profile_updated_at` INTEGER NOT NULL)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_UserEmail` ON `users` (`UserEmail`)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_firebase_uid` ON `users` (`firebase_uid`)");
        }
    };
    public abstract PostDao postDao();
    public abstract UserDao userDao();

    public static BlogRoomDatabase get(Context context) {
        if (instance == null) {
            synchronized (BlogRoomDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            BlogRoomDatabase.class, "dogsit_room.db")
                            .addMigrations(MIGRATION_1_2).build();
                }
            }
        }
        return instance;
    }
}
