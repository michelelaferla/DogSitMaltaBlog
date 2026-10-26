package com.example.dogsitmalta.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.model.User;
import com.example.dogsitmalta.utils.PasswordHasher;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Local source of truth, with additive migrations from supplied v1 schema. */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "dogsit_blog.db";
    public static final int DATABASE_VERSION = 3;
    public static final String TABLE_POSTS = "posts";
    public static final String TABLE_USERS = "users";
    private static final String ID = "id", TITLE = "title", MESSAGE = "message";
    private static final String LOCATION = "location", IMAGE = "image_path";
    private static final String CREATED = "created_at", UPDATED = "updated_at";
    private static final String LAT = "latitude", LNG = "longitude";
    private static final String REMOTE = "remote_id", OWNER = "owner_uid";
    private static final String DIRTY = "is_dirty", DELETED = "is_deleted";

    public DatabaseHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }
    @Override public void onConfigure(SQLiteDatabase db) { super.onConfigure(db); db.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE posts (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, " +
                "message TEXT NOT NULL, location TEXT, image_path TEXT, created_at INTEGER NOT NULL, " +
                "updated_at INTEGER NOT NULL, latitude REAL, longitude REAL, remote_id TEXT UNIQUE, " +
                "owner_uid TEXT, is_dirty INTEGER NOT NULL DEFAULT 1, is_deleted INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX idx_posts_title ON posts(title)");
        db.execSQL("CREATE INDEX idx_posts_created_at ON posts(created_at)");
        createUsers(db);
    }
    private void createUsers(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "UserName TEXT NOT NULL, UserSurname TEXT NOT NULL, UserEmail TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "UserPassword TEXT, UserDateCreated INTEGER NOT NULL, UserDateUpdated INTEGER NULL, " +
                "UserIsActive INTEGER NOT NULL DEFAULT 1 CHECK(UserIsActive IN (0,1)), " +
                "firebase_uid TEXT UNIQUE, profile_updated_at INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) createUsers(db);
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE posts ADD COLUMN latitude REAL");
            db.execSQL("ALTER TABLE posts ADD COLUMN longitude REAL");
            db.execSQL("ALTER TABLE posts ADD COLUMN remote_id TEXT");
            db.execSQL("CREATE UNIQUE INDEX idx_posts_remote_id ON posts(remote_id)");
            db.execSQL("ALTER TABLE posts ADD COLUMN owner_uid TEXT");
            db.execSQL("ALTER TABLE posts ADD COLUMN is_dirty INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE posts ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0");
        }
    }
    private ContentValues postValues(BlogPost post, long now, boolean creating) {
        ContentValues v = new ContentValues();
        v.put(TITLE,post.getTitle()); v.put(MESSAGE,post.getMessage());
        v.put(LOCATION,post.getLocation()); v.put(IMAGE,post.getImagePath());
        if (post.getLatitude() == null) v.putNull(LAT); else v.put(LAT,post.getLatitude());
        if (post.getLongitude() == null) v.putNull(LNG); else v.put(LNG,post.getLongitude());
        v.put(UPDATED,now); v.put(DIRTY,1); v.put(DELETED,post.isDeleted()?1:0);
        if (creating) { v.put(CREATED,now); v.put(REMOTE,UUID.randomUUID().toString()); v.put(OWNER,post.getOwnerUid()); }
        return v;
    }
    public long insertPost(BlogPost post) {
        long now = System.currentTimeMillis();
        return getWritableDatabase().insertOrThrow(TABLE_POSTS,null,postValues(post,now,true));
    }
    public int updatePost(BlogPost post) {
        return getWritableDatabase().update(TABLE_POSTS,postValues(post,System.currentTimeMillis(),false),
                ID+" = ? AND is_deleted = 0",new String[]{String.valueOf(post.getId())});
    }
    public BlogPost getPost(long id) {
        try (Cursor c=getReadableDatabase().query(TABLE_POSTS,null,"id = ? AND is_deleted = 0",
                new String[]{String.valueOf(id)},null,null,null)) {
            return c.moveToFirst()?fromPost(c):null;
        }
    }
    public List<BlogPost> getAllPosts() { return listPosts("is_deleted = 0",null); }
    public List<BlogPost> searchPosts(String text) {
        if (text==null || text.trim().isEmpty()) return getAllPosts();
        String term="%"+text.trim()+"%";
        return listPosts("is_deleted = 0 AND (title LIKE ? COLLATE NOCASE OR " +
                "message LIKE ? COLLATE NOCASE OR location LIKE ? COLLATE NOCASE)",new String[]{term,term,term});
    }
    private List<BlogPost> listPosts(String where,String[] args) {
        List<BlogPost> out=new ArrayList<>();
        try (Cursor c=getReadableDatabase().query(TABLE_POSTS,null,where,args,null,null,"created_at DESC")) {
            while(c.moveToNext()) out.add(fromPost(c));
        }
        return out;
    }
    private BlogPost fromPost(Cursor c) {
        BlogPost p=new BlogPost();
        p.setId(c.getLong(c.getColumnIndexOrThrow(ID)));
        p.setTitle(c.getString(c.getColumnIndexOrThrow(TITLE)));
        p.setMessage(c.getString(c.getColumnIndexOrThrow(MESSAGE)));
        p.setLocation(c.getString(c.getColumnIndexOrThrow(LOCATION)));
        p.setImagePath(c.getString(c.getColumnIndexOrThrow(IMAGE)));
        p.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(CREATED)));
        p.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(UPDATED)));
        int li=c.getColumnIndexOrThrow(LAT),gi=c.getColumnIndexOrThrow(LNG);
        p.setLatitude(c.isNull(li)?null:c.getDouble(li));
        p.setLongitude(c.isNull(gi)?null:c.getDouble(gi));
        p.setRemoteId(c.getString(c.getColumnIndexOrThrow(REMOTE)));
        p.setOwnerUid(c.getString(c.getColumnIndexOrThrow(OWNER)));
        p.setDirty(c.getInt(c.getColumnIndexOrThrow(DIRTY))==1);
        p.setDeleted(c.getInt(c.getColumnIndexOrThrow(DELETED))==1);
        return p;
    }
    public int deletePost(long id) { return markDeleted(id); }
    private int markDeleted(long id) {
        ContentValues v=new ContentValues();v.put(DELETED,1);v.put(DIRTY,1);v.put(UPDATED,System.currentTimeMillis());
        return getWritableDatabase().update(TABLE_POSTS,v,"id = ? AND is_deleted = 0",new String[]{String.valueOf(id)});
    }
    public void deletePosts(List<Long> ids) {
        if (ids==null || ids.isEmpty())return;
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();
        try {for(Long id:ids) if(id!=null)markDeleted(id);db.setTransactionSuccessful();}
        finally {db.endTransaction();}
    }
    public List<BlogPost> getPendingPostsForSync(String uid) {
        List<BlogPost> out=listPosts("is_dirty = 1 AND (owner_uid = ? OR owner_uid IS NULL)",new String[]{uid});
        for(BlogPost p:out) {
            if(p.getRemoteId()==null) {
                String newId=UUID.randomUUID().toString();ContentValues v=new ContentValues();v.put(REMOTE,newId);
                getWritableDatabase().update(TABLE_POSTS,v,"id = ?",new String[]{String.valueOf(p.getId())});
                p.setRemoteId(newId);
            }
        }
        return out;
    }
    public void markPostSynced(long id,long knownUpdatedAt,String uid) {
        ContentValues v=new ContentValues();v.put(DIRTY,0);v.put(OWNER,uid);
        getWritableDatabase().update(TABLE_POSTS,v,"id = ? AND updated_at = ?",
                new String[]{String.valueOf(id),String.valueOf(knownUpdatedAt)});
    }
    /** Server-side current values win only for clean local rows; tombstones are retained. */
    public void upsertCloudPost(BlogPost incoming,String uid) {
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.query(TABLE_POSTS,new String[]{ID,UPDATED,DIRTY},"remote_id = ?",
                new String[]{incoming.getRemoteId()},null,null,null)) {
            if(c.moveToFirst()) {
                long id=c.getLong(0),stamp=c.getLong(1); boolean dirty=c.getInt(2)!=0;
                if(dirty || stamp>incoming.getUpdatedAt())return;
                ContentValues v=postValues(incoming,incoming.getUpdatedAt(),false);
                v.put(CREATED,incoming.getCreatedAt());v.put(DIRTY,0);v.put(OWNER,uid);
                v.remove(IMAGE); // preserve any local image path during metadata refresh
                db.update(TABLE_POSTS,v,"id = ?",new String[]{String.valueOf(id)});
                return;
            }
        }
        ContentValues v=postValues(incoming,incoming.getUpdatedAt(),true);
        v.put(CREATED,incoming.getCreatedAt());v.put(REMOTE,incoming.getRemoteId());
        v.put(OWNER,uid);v.put(DIRTY,0);v.putNull(IMAGE);
        db.insertWithOnConflict(TABLE_POSTS,null,v,SQLiteDatabase.CONFLICT_IGNORE);
    }
    public long insertUser(User u) {
        long now=System.currentTimeMillis();ContentValues v=userValues(u);
        v.put("UserDateCreated",now);v.putNull("UserDateUpdated");v.put("profile_updated_at",now);
        return getWritableDatabase().insertOrThrow(TABLE_USERS,null,v);
    }
    private ContentValues userValues(User u) {
        ContentValues v=new ContentValues();v.put("UserName",u.getUserName());v.put("UserSurname",u.getUserSurname());
        v.put("UserEmail",u.getUserEmail());
        if(u.getUserPassword()==null)v.putNull("UserPassword"); else v.put("UserPassword",u.getUserPassword());
        v.put("UserIsActive",u.isUserIsActive()?1:0);
        if(u.getFirebaseUid()==null)v.putNull("firebase_uid");else v.put("firebase_uid",u.getFirebaseUid());
        return v;
    }
    public User findUser(String email) {
        try(Cursor c=getReadableDatabase().query(TABLE_USERS,null,"UserEmail = ?",
                new String[]{email},null,null,null)) {return c.moveToFirst()?fromUser(c):null;}
    }
    public User findUserByUid(String uid) {
        try(Cursor c=getReadableDatabase().query(TABLE_USERS,null,"firebase_uid = ?",
                new String[]{uid},null,null,null)) {return c.moveToFirst()?fromUser(c):null;}
    }
    private User fromUser(Cursor c) {
        User u=new User();u.setUserID(c.getLong(c.getColumnIndexOrThrow("UserID")));
        u.setUserName(c.getString(c.getColumnIndexOrThrow("UserName")));
        u.setUserSurname(c.getString(c.getColumnIndexOrThrow("UserSurname")));
        u.setUserEmail(c.getString(c.getColumnIndexOrThrow("UserEmail")));
        u.setUserPassword(c.getString(c.getColumnIndexOrThrow("UserPassword")));
        u.setUserDateCreated(c.getLong(c.getColumnIndexOrThrow("UserDateCreated")));
        int change=c.getColumnIndexOrThrow("UserDateUpdated");u.setUserDateUpdated(c.isNull(change)?null:c.getLong(change));
        u.setUserIsActive(c.getInt(c.getColumnIndexOrThrow("UserIsActive"))==1);
        u.setFirebaseUid(c.getString(c.getColumnIndexOrThrow("firebase_uid")));
        u.setProfileUpdatedAt(c.getLong(c.getColumnIndexOrThrow("profile_updated_at")));
        return u;
    }
    public User authenticateLocal(String email,char[] password) {
        User u=findUser(email);
        return u!=null && u.isUserIsActive() && PasswordHasher.verify(password,u.getUserPassword()) ? u:null;
    }
    public void attachFirebaseUid(String email,String uid) {
        ContentValues v=new ContentValues();v.put("firebase_uid",uid);
        getWritableDatabase().update(TABLE_USERS,v,"UserEmail = ?",new String[]{email});
    }
    /** Cloud profile does not contain UserPassword; never overwrite local stored hash. */
    public void upsertCloudUser(User u) {
        User previous=findUser(u.getUserEmail());
        if(previous!=null && previous.getProfileUpdatedAt() > u.getProfileUpdatedAt())return;
        ContentValues v=new ContentValues();v.put("UserName",u.getUserName());v.put("UserSurname",u.getUserSurname());
        v.put("UserEmail",u.getUserEmail());v.put("UserIsActive",u.isUserIsActive()?1:0);
        v.put("firebase_uid",u.getFirebaseUid());v.put("profile_updated_at",u.getProfileUpdatedAt());
        if(previous!=null) {
            getWritableDatabase().update(TABLE_USERS,v,"UserID = ?",new String[]{String.valueOf(previous.getUserID())});
        }else {
            v.putNull("UserPassword");v.put("UserDateCreated",u.getUserDateCreated());v.putNull("UserDateUpdated");
            getWritableDatabase().insertWithOnConflict(TABLE_USERS,null,v,SQLiteDatabase.CONFLICT_IGNORE);
        }
    }
    public boolean updatePassword(String email,String newHash) {
        ContentValues v=new ContentValues();v.put("UserPassword",newHash);
        v.put("UserDateUpdated",System.currentTimeMillis());
        return getWritableDatabase().update(TABLE_USERS,v,"UserEmail = ?",new String[]{email})>0;
    }
}
