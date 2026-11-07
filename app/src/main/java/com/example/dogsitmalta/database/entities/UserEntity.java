package com.example.dogsitmalta.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.example.dogsitmalta.model.User;

/** Stored account profile. Never upload the password verifier to Firestore. */
@Entity(tableName = "users", indices = {
        @Index(value = "UserEmail", unique = true),
        @Index(value = "firebase_uid", unique = true)})
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "UserID") public long id;
    @NonNull @ColumnInfo(name = "UserName") public String name = "";
    @NonNull @ColumnInfo(name = "UserSurname") public String surname = "";
    @NonNull @ColumnInfo(name = "UserEmail", collate = ColumnInfo.NOCASE) public String email = "";
    @ColumnInfo(name = "UserPassword") public String passwordHash;
    @ColumnInfo(name = "UserDateCreated") public long createdAt;
    @ColumnInfo(name = "UserDateUpdated") public Long passwordUpdatedAt;
    @ColumnInfo(name = "UserIsActive") public boolean active = true;
    @ColumnInfo(name = "firebase_uid") public String firebaseUid;
    @ColumnInfo(name = "profile_updated_at") public long profileUpdatedAt;

    public static UserEntity from(User u) {
        UserEntity row = new UserEntity();
        row.id = u.getUserID(); row.name = u.getUserName();
        row.surname = u.getUserSurname(); row.email = u.getUserEmail();
        row.passwordHash = u.getUserPassword(); row.createdAt = u.getUserDateCreated();
        row.passwordUpdatedAt = u.getUserDateUpdated(); row.active = u.isUserIsActive();
        row.firebaseUid = u.getFirebaseUid(); row.profileUpdatedAt = u.getProfileUpdatedAt();
        return row;
    }
    public User toModel() {
        User u = new User(); u.setUserID(id); u.setUserName(name);
        u.setUserSurname(surname); u.setUserEmail(email);
        u.setUserPassword(passwordHash); u.setUserDateCreated(createdAt);
        u.setUserDateUpdated(passwordUpdatedAt); u.setUserIsActive(active);
        u.setFirebaseUid(firebaseUid); u.setProfileUpdatedAt(profileUpdatedAt);
        return u;
    }
}
