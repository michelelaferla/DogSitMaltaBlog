package com.example.dogsitmalta.sync;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dogsitmalta.database.repos.RoomPostRepository;
import com.example.dogsitmalta.database.repos.RoomUserRepository;
import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.model.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented integration tests for local repository sync reconciliation logic.
 * Executes on Android runtime environment against an in-memory database to test
 * dirty flags, pending posts retrieval, and cloud profile merging.
 */
@RunWith(AndroidJUnit4.class)
public class SyncManagerInstrumentedTest {

    private Context context;
    private RoomPostRepository postRepository;
    private RoomUserRepository userRepository;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        postRepository = new RoomPostRepository(context);
        userRepository = new RoomUserRepository(context);
    }

    @Test
    public void testPendingPostsQuery_retrievesDirtyPostsForSync() {
        BlogPost post = new BlogPost();
        post.setTitle("Sync Test Post");
        post.setMessage("Pending sync message");
        post.setLocation("Sliema");
        post.setOwnerUid("user_uid_100");

        long id = postRepository.insert(post);
        assertTrue("Inserted post ID should be positive", id > 0);

        List<BlogPost> pending = postRepository.pending("user_uid_100");
        assertNotNull(pending);
        assertTrue("Pending posts list should contain inserted dirty post", pending.size() > 0);
    }

    @Test
    public void testMarkSynced_clearsDirtyFlagInLocalDatabase() {
        BlogPost post = new BlogPost();
        post.setTitle("Post To Be Synced");
        post.setMessage("Content");
        post.setOwnerUid("user_uid_200");

        long id = postRepository.insert(post);
        BlogPost inserted = postRepository.get(id);
        assertNotNull(inserted);

        // Mark post as synced
        postRepository.markSynced(id, inserted.getUpdatedAt(), "user_uid_200");

        // Pending list for user should no longer contain this post
        List<BlogPost> pendingAfter = postRepository.pending("user_uid_200");
        boolean stillPending = false;
        for (BlogPost p : pendingAfter) {
            if (p.getId() == id) {
                stillPending = true;
                break;
            }
        }
        assertTrue("Marking post as synced should remove it from pending sync queue", !stillPending);
    }

    @Test
    public void testUpsertCloudUser_mergesRemoteProfileInfo() {
        String email = "cloud_profile_" + System.currentTimeMillis() + "@example.com";
        User cloudProfile = new User();
        cloudProfile.setUserEmail(email);
        cloudProfile.setUserName("CloudFirst");
        cloudProfile.setUserSurname("CloudLast");
        cloudProfile.setFirebaseUid("uid_cloud_300");
        cloudProfile.setUserIsActive(true);
        cloudProfile.setProfileUpdatedAt(System.currentTimeMillis());

        userRepository.upsertCloudUser(cloudProfile);

        User loadedUser = userRepository.findUserByUid("uid_cloud_300");
        assertNotNull("Cloud profile should be merged into local user table", loadedUser);
        assertEquals("CloudFirst", loadedUser.getUserName());
        assertEquals("CloudLast", loadedUser.getUserSurname());
    }
}
