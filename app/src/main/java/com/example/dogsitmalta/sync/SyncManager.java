package com.example.dogsitmalta.sync;

import android.content.Context;
import android.util.Log;
import com.example.dogsitmalta.database.DatabaseHelper;
import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Demonstration two-way sync: local SQLite remains primary, with cloud owner-scoped copies.
 * Not a production synchronization engine: concurrent edits use updated_at (last-writer-wins).
 * Image files remain on the device; coordinate fields ARE synchronized. */
public final class SyncManager {
    private static final String TAG="DogSitSync";
    private final Context appContext;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    public SyncManager(Context context) { appContext=context.getApplicationContext(); }
    public static boolean isConfigured(Context context) {
        return !FirebaseApp.getApps(context.getApplicationContext()).isEmpty();
    }
    public static FirebaseUser currentUser(Context context) {
        return isConfigured(context)?FirebaseAuth.getInstance().getCurrentUser():null;
    }
    public void sync(Runnable uiAfterSync) {
        FirebaseUser me=currentUser(appContext);
        if(me==null)return;
        String uid=me.getUid();
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        DatabaseHelper local=new DatabaseHelper(appContext);
        worker.execute(()-> {
            List<BlogPost> pending=local.getPendingPostsForSync(uid);
            // Reconcile the USERS table before the POSTS table.
            // A server profile with a later profile_updated_at wins over stale local data.
            reconcileProfile(firestore, uid, local);
            if(pending.isEmpty()) { download(firestore,uid,local,uiAfterSync);return; }
            AtomicInteger remaining=new AtomicInteger(pending.size());
            for(BlogPost p:pending) {
                Map<String,Object> document=new HashMap<>();
                document.put("title",p.getTitle());document.put("message",p.getMessage());
                document.put("location",p.getLocation());document.put("latitude",p.getLatitude());
                document.put("longitude",p.getLongitude());
                document.put("created_at",p.getCreatedAt());document.put("updated_at",p.getUpdatedAt());
                document.put("is_deleted",p.isDeleted());document.put("owner_uid",uid);
                firestore.collection("users").document(uid).collection("posts")
                  .document(p.getRemoteId()).set(document).addOnCompleteListener(result->{
                      if(result.isSuccessful()) worker.execute(()->local.markPostSynced(p.getId(),p.getUpdatedAt(),uid));
                      else Log.w(TAG,"Post sync upload failed",result.getException());
                      if(remaining.decrementAndGet()==0)download(firestore,uid,local,uiAfterSync);
                  });
            }
        });
    }
    private void reconcileProfile(FirebaseFirestore db, String uid, DatabaseHelper local) {
        db.collection("users").document(uid).get(Source.SERVER)
            .addOnSuccessListener(profile -> worker.execute(() -> {
                User current = local.findUserByUid(uid);
                if (profile.exists()) {
                    User incoming = new User();incoming.setFirebaseUid(uid);
                    incoming.setUserName(profile.getString("UserName"));
                    incoming.setUserSurname(profile.getString("UserSurname"));
                    incoming.setUserEmail(profile.getString("UserEmail"));
                    incoming.setUserIsActive(!Boolean.FALSE.equals(profile.getBoolean("UserIsActive")));
                    Long created=profile.getLong("UserDateCreated"), changed=profile.getLong("profile_updated_at");
                    incoming.setUserDateCreated(created == null ? System.currentTimeMillis() : created);
                    incoming.setProfileUpdatedAt(changed == null ? incoming.getUserDateCreated() : changed);
                    if (incoming.getUserEmail()!=null && incoming.getUserName()!=null && incoming.getUserSurname()!=null
                            && (current==null || current.getProfileUpdatedAt() < incoming.getProfileUpdatedAt())) {
                        local.upsertCloudUser(incoming);
                        return;
                    }
                }
                if (current != null) uploadProfile(db, uid, current);
            }))
            .addOnFailureListener(err -> Log.w(TAG,"Profile fetch failed; retry later",err));
    }
    private void uploadProfile(FirebaseFirestore db,String uid,User localUser) {
        Map<String,Object> profile=new HashMap<>();
        profile.put("UserName",localUser.getUserName());
        profile.put("UserSurname",localUser.getUserSurname());
        profile.put("UserEmail",localUser.getUserEmail());
        profile.put("UserIsActive",localUser.isUserIsActive());
        profile.put("UserDateCreated",localUser.getUserDateCreated());
        profile.put("UserDateUpdated",localUser.getUserDateUpdated());
        profile.put("profile_updated_at",localUser.getProfileUpdatedAt());
        // Never send UserPassword, even when it contains a PBKDF2 verifier hash.
        db.collection("users").document(uid).set(profile)
            .addOnFailureListener(err->Log.w(TAG,"Profile upload failed",err));
    }
    private void download(FirebaseFirestore db,String uid,DatabaseHelper local,Runnable uiAfterSync) {
        db.collection("users").document(uid).collection("posts").get(Source.SERVER)
            .addOnSuccessListener(results->worker.execute(()-> {
                for(DocumentSnapshot doc:results.getDocuments()) {
                    String title=doc.getString("title"),message=doc.getString("message");
                    if(title==null||message==null)continue;
                    BlogPost p=new BlogPost();p.setRemoteId(doc.getId());p.setOwnerUid(uid);
                    p.setTitle(title);p.setMessage(message);p.setLocation(doc.getString("location"));
                    p.setLatitude(doc.getDouble("latitude"));p.setLongitude(doc.getDouble("longitude"));
                    Long created=doc.getLong("created_at"),updated=doc.getLong("updated_at");
                    p.setCreatedAt(created==null?System.currentTimeMillis():created);
                    p.setUpdatedAt(updated==null?p.getCreatedAt():updated);
                    p.setDeleted(Boolean.TRUE.equals(doc.getBoolean("is_deleted")));
                    local.upsertCloudPost(p,uid);
                }
                local.close();
                if(uiAfterSync!=null)new android.os.Handler(android.os.Looper.getMainLooper()).post(uiAfterSync);
            }))
            .addOnFailureListener(e->{Log.w(TAG,"Download failed",e);local.close();});
    }
}
