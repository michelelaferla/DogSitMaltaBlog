package com.example.dogsitmalta.database.repos;

import android.content.Context;

import com.example.dogsitmalta.database.BlogRoomDatabase;
import com.example.dogsitmalta.database.LegacyRoomImporter;
import com.example.dogsitmalta.database.entities.UserEntity;
import com.example.dogsitmalta.database.interfaces.UserDao;
import com.example.dogsitmalta.model.User;
import com.example.dogsitmalta.utils.PasswordHasher;

/** Room-backed user registration, login and profile sync; call on worker threads. */
public final class RoomUserRepository {
    private final Context app;
    private final BlogRoomDatabase database;
    private final UserDao dao;
    public RoomUserRepository(Context context) {
        app = context.getApplicationContext();
        database = BlogRoomDatabase.get(app);
        dao = database.userDao();
    }
    private void ready() { LegacyRoomImporter.importIfNeeded(app); }
    public User findUser(String email) {
        ready(); UserEntity row = dao.findByEmail(email);
        return row == null ? null : row.toModel();
    }
    public User findUserByUid(String uid) {
        ready(); UserEntity row = dao.findByUid(uid);
        return row == null ? null : row.toModel();
    }
    public long insertUser(User user) {
        ready();
        long now = System.currentTimeMillis();
        UserEntity row = UserEntity.from(user);
        row.id = 0; row.createdAt = now; row.passwordUpdatedAt = null;
        row.profileUpdatedAt = now;
        long id = dao.insert(row);
        if (id == -1) throw new IllegalStateException("Email already registered");
        return id;
    }
    public User authenticateLocal(String email, char[] password) {
        User user = findUser(email);
        return user != null && user.isUserIsActive() &&
                PasswordHasher.verify(password, user.getUserPassword()) ? user : null;
    }
    public void attachFirebaseUid(String email, String uid) {
        ready(); dao.attachFirebaseUid(email, uid);
    }
    public boolean updatePassword(String email, String hash) {
        ready(); return dao.updatePassword(email, hash, System.currentTimeMillis()) > 0;
    }
    /** Merge remote public profile; preserve the local password verifier. */
    public void upsertCloudUser(User incoming) {
        ready();
        database.runInTransaction(() -> {
            UserEntity previous = dao.findByEmail(incoming.getUserEmail());
            if (previous == null && incoming.getFirebaseUid() != null)
                previous = dao.findByUid(incoming.getFirebaseUid());
            if (previous != null && previous.profileUpdatedAt > incoming.getProfileUpdatedAt()) return;
            if (previous == null) {
                UserEntity row = UserEntity.from(incoming);
                row.id = 0; row.passwordHash = null;
                dao.insert(row);
            } else {
                previous.name = incoming.getUserName();
                previous.surname = incoming.getUserSurname();
                previous.email = incoming.getUserEmail();
                previous.active = incoming.isUserIsActive();
                previous.firebaseUid = incoming.getFirebaseUid();
                previous.profileUpdatedAt = incoming.getProfileUpdatedAt();
                dao.update(previous);
            }
        });
    }
}
