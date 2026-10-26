package com.example.dogsitmalta.model;

public class BlogPost {

    private long id;
    private String title;
    private String message;
    private String location;
    private String imagePath;
    private long createdAt;
    private long updatedAt;
    private Double latitude;
    private Double longitude;
    private String remoteId;
    private String ownerUid;
    private boolean deleted;
    private boolean dirty = true;

    public BlogPost() {
    }

    public BlogPost(long id, String title, String message, String location,
                    String imagePath, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.location = location;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double value) { latitude = value; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double value) { longitude = value; }
    public String getRemoteId() { return remoteId; }
    public void setRemoteId(String value) { remoteId = value; }
    public String getOwnerUid() { return ownerUid; }
    public void setOwnerUid(String value) { ownerUid = value; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean value) { deleted = value; }
    public boolean isDirty() { return dirty; }
    public void setDirty(boolean value) { dirty = value; }
}
