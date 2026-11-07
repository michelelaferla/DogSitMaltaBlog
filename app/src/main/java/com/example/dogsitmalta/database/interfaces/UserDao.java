package com.example.dogsitmalta.database.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dogsitmalta.database.entities.UserEntity;

/** All user-supplied values are bound as SQL parameters. */
@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) long insert(UserEntity user);
    @Update int update(UserEntity user);
    @Query("SELECT * FROM users WHERE UserEmail = :email COLLATE NOCASE LIMIT 1")
    UserEntity findByEmail(String email);
    @Query("SELECT * FROM users WHERE firebase_uid = :uid LIMIT 1")
    UserEntity findByUid(String uid);
    @Query("UPDATE users SET firebase_uid = :uid WHERE UserEmail = :email COLLATE NOCASE")
    int attachFirebaseUid(String email, String uid);
    @Query("UPDATE users SET UserPassword = :hash, UserDateUpdated = :updatedAt " +
           "WHERE UserEmail = :email COLLATE NOCASE")
    int updatePassword(String email, String hash, long updatedAt);
}
