package com.example.dogsitmalta.database.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dogsitmalta.database.entities.PostEntity;

import java.util.List;

/** Room binds every :parameter; user input cannot modify SQL structure. */
@Dao
public interface PostDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) long insert(PostEntity row);
    @Update int update(PostEntity row);

    @Query("SELECT * FROM posts WHERE id = :id AND is_deleted = 0 LIMIT 1")
    PostEntity findVisible(long id);

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    PostEntity findAny(long id);

    @Query("SELECT * FROM posts WHERE remote_id = :remoteId LIMIT 1")
    PostEntity findByRemoteId(String remoteId);

    @Query("SELECT * FROM posts WHERE is_deleted = 0 ORDER BY created_at DESC")
    List<PostEntity> allVisible();

    @Query("SELECT * FROM posts WHERE is_deleted = 0 AND (title LIKE '%' || :term || '%' " +
           "OR message LIKE '%' || :term || '%' OR location LIKE '%' || :term || '%') " +
           "ORDER BY created_at DESC")
    List<PostEntity> search(String term);

    @Query("SELECT * FROM posts WHERE is_dirty = 1 AND (owner_uid = :uid OR owner_uid IS NULL)")
    List<PostEntity> pending(String uid);

    @Query("UPDATE posts SET is_deleted = 1, is_dirty = 1, updated_at = :now " +
           "WHERE id = :id AND is_deleted = 0")
    int softDelete(long id, long now);

    @Query("UPDATE posts SET is_dirty = 0, owner_uid = :uid " +
           "WHERE id = :id AND updated_at = :stamp")
    void markSynced(long id, long stamp, String uid);

    @Query("UPDATE posts SET remote_id = :remoteId WHERE id = :id")
    void setRemoteId(long id, String remoteId);
}
