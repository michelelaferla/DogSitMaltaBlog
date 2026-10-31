package com.example.dogsitmalta.model;

/** Local account profile. UserPassword holds a salted PBKDF2 HASH, never plain text.
 * Password hashes must never be uploaded to Firestore. */
public class User {
    private long userID;
    private String userName, userSurname, userEmail, userPassword, firebaseUid;
    private long userDateCreated;
    private Long userDateUpdated; // null until password changed
    private boolean userIsActive = true;
    private long profileUpdatedAt;
    public long getUserID() { return userID; }
    public void setUserID(long value) { userID = value; }
    public String getUserName() { return userName; }
    public void setUserName(String value) { userName = value; }
    public String getUserSurname() { return userSurname; }
    public void setUserSurname(String value) { userSurname = value; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String value) { userEmail = value; }
    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String hashOnly) { userPassword = hashOnly; }
    public long getUserDateCreated() { return userDateCreated; }
    public void setUserDateCreated(long value) { userDateCreated = value; }
    public Long getUserDateUpdated() { return userDateUpdated; }
    public void setUserDateUpdated(Long value) { userDateUpdated = value; }
    public boolean isUserIsActive() { return userIsActive; }
    public void setUserIsActive(boolean value) { userIsActive = value; }
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String value) { firebaseUid = value; }
    public long getProfileUpdatedAt() { return profileUpdatedAt; }
    public void setProfileUpdatedAt(long value) { profileUpdatedAt = value; }
}
