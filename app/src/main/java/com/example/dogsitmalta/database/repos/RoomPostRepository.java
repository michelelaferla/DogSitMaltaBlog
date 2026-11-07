package com.example.dogsitmalta.database.repos;

import android.content.Context;

import com.example.dogsitmalta.database.BlogRoomDatabase;
import com.example.dogsitmalta.database.LegacyRoomImporter;
import com.example.dogsitmalta.database.entities.PostEntity;
import com.example.dogsitmalta.database.interfaces.PostDao;
import com.example.dogsitmalta.model.BlogPost;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Post repository backed solely by Room.
 * All methods must be called on background threads (never on the Android UI thread).
 */
public final class RoomPostRepository {
    private final Context app;
    private final BlogRoomDatabase room;
    private final PostDao dao;

    public RoomPostRepository(Context context) {
        app = context.getApplicationContext();
        room = BlogRoomDatabase.get(app);
        dao = room.postDao();
    }

    private void importLegacyIfNeeded() { LegacyRoomImporter.importIfNeeded(app); }
    private List<BlogPost> models(List<PostEntity> rows) {
        List<BlogPost> result = new ArrayList<>();
        for (PostEntity row : rows) result.add(row.toModel());
        return result;
    }
    public List<BlogPost> search(String term) {
        importLegacyIfNeeded();
        return models(term == null || term.trim().isEmpty() ? dao.allVisible() : dao.search(term.trim()));
    }
    public BlogPost get(long id) {
        importLegacyIfNeeded();
        PostEntity p = dao.findVisible(id);
        return p == null ? null : p.toModel();
    }
    public long insert(BlogPost p) {
        importLegacyIfNeeded();
        long now = System.currentTimeMillis();
        PostEntity row = PostEntity.from(p);
        row.id = 0; row.createdAt = now; row.updatedAt = now;
        row.remoteId = UUID.randomUUID().toString(); row.dirty = true;
        return dao.insert(row);
    }
    public int update(BlogPost p) {
        importLegacyIfNeeded();
        PostEntity old = dao.findVisible(p.getId());
        if (old == null) return 0;
        old.title = p.getTitle(); old.message = p.getMessage();
        old.location = p.getLocation(); old.imagePath = p.getImagePath();
        old.latitude = p.getLatitude(); old.longitude = p.getLongitude();
        old.updatedAt = System.currentTimeMillis(); old.dirty = true;
        // Preserve ownerUid / remoteId / createdAt as SQLiteOpenHelper implementation did.
        return dao.update(old);
    }
    public int delete(long id) {
        importLegacyIfNeeded();
        return dao.softDelete(id, System.currentTimeMillis());
    }
    public void deleteMany(List<Long> ids) {
        importLegacyIfNeeded();
        room.runInTransaction(() -> {
            for (Long id : ids) if (id != null) dao.softDelete(id, System.currentTimeMillis());
        });
    }
    public List<BlogPost> pending(String uid) {
        importLegacyIfNeeded();
        List<PostEntity> rows = dao.pending(uid);
        for (PostEntity row : rows) {
            if (row.remoteId == null) {
                row.remoteId = UUID.randomUUID().toString();
                dao.setRemoteId(row.id, row.remoteId);
            }
        }
        return models(rows);
    }
    public void markSynced(long id, long stamp, String uid) {
        importLegacyIfNeeded(); dao.markSynced(id, stamp, uid);
    }
    public void upsertCloud(BlogPost incoming, String uid) {
        importLegacyIfNeeded();
        if (incoming.getRemoteId() == null) return;
        room.runInTransaction(() -> {
            PostEntity old = dao.findByRemoteId(incoming.getRemoteId());
            if (old != null) {
                if (old.dirty || old.updatedAt > incoming.getUpdatedAt()) return;
                old.title = incoming.getTitle(); old.message = incoming.getMessage();
                old.location = incoming.getLocation(); old.latitude = incoming.getLatitude();
                old.longitude = incoming.getLongitude(); old.createdAt = incoming.getCreatedAt();
                old.updatedAt = incoming.getUpdatedAt(); old.deleted = incoming.isDeleted();
                old.ownerUid = uid; old.dirty = false;
                // Preserve the local image path; cloud only carries metadata.
                dao.update(old);
            } else {
                PostEntity row = PostEntity.from(incoming);
                row.id = 0; row.imagePath = null; row.ownerUid = uid; row.dirty = false;
                dao.insert(row);
            }
        });
    }
}
