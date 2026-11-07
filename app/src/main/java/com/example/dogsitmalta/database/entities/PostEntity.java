package com.example.dogsitmalta.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.example.dogsitmalta.model.BlogPost;

/** SQLite row stored by Room; the UI continues using the existing BlogPost model. */
@Entity(tableName = "posts", indices = {
        @Index(value = "title"), @Index(value = "created_at"),
        @Index(value = "remote_id", unique = true)
})
public class PostEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String title = "";
    @NonNull public String message = "";
    public String location;
    @ColumnInfo(name = "image_path") public String imagePath;
    @ColumnInfo(name = "created_at") public long createdAt;
    @ColumnInfo(name = "updated_at") public long updatedAt;
    public Double latitude;
    public Double longitude;
    @ColumnInfo(name = "remote_id") public String remoteId;
    @ColumnInfo(name = "owner_uid") public String ownerUid;
    @ColumnInfo(name = "is_dirty") public boolean dirty = true;
    @ColumnInfo(name = "is_deleted") public boolean deleted;

    public static PostEntity from(BlogPost p) {
        PostEntity e = new PostEntity();
        e.id = p.getId(); e.title = p.getTitle(); e.message = p.getMessage();
        e.location = p.getLocation(); e.imagePath = p.getImagePath();
        e.createdAt = p.getCreatedAt(); e.updatedAt = p.getUpdatedAt();
        e.latitude = p.getLatitude(); e.longitude = p.getLongitude();
        e.remoteId = p.getRemoteId(); e.ownerUid = p.getOwnerUid();
        e.deleted = p.isDeleted(); return e;
    }
    public BlogPost toModel() {
        BlogPost p = new BlogPost();
        p.setId(id); p.setTitle(title); p.setMessage(message);
        p.setLocation(location); p.setImagePath(imagePath);
        p.setCreatedAt(createdAt); p.setUpdatedAt(updatedAt);
        p.setLatitude(latitude); p.setLongitude(longitude);
        p.setRemoteId(remoteId); p.setOwnerUid(ownerUid);
        p.setDeleted(deleted); return p;
    }
}
