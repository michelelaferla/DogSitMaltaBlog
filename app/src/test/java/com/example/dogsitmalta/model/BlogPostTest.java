package com.example.dogsitmalta.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link BlogPost} domain model class.
 * Tests object construction, getter/setter operations, property updates,
 * null/empty string handling, boundary values, and coordinate assignments.
 */
public class BlogPostTest {

    private BlogPost post;

    @Before
    public void setUp() {
        post = new BlogPost();
    }

    @Test
    public void testDefaultConstructor_initializesDefaultValues() {
        BlogPost emptyPost = new BlogPost();
        assertEquals(0L, emptyPost.getId());
        assertNull(emptyPost.getTitle());
        assertNull(emptyPost.getMessage());
        assertNull(emptyPost.getLocation());
        assertNull(emptyPost.getImagePath());
        assertEquals(0L, emptyPost.getCreatedAt());
        assertEquals(0L, emptyPost.getUpdatedAt());
        assertNull(emptyPost.getLatitude());
        assertNull(emptyPost.getLongitude());
        assertNull(emptyPost.getRemoteId());
        assertNull(emptyPost.getOwnerUid());
        assertFalse(emptyPost.isDeleted());
    }

    @Test
    public void testParameterizedConstructor_setsInitialFieldsCorrectly() {
        long id = 101L;
        String title = "Dog Walking in Sliema";
        String message = "Looking for a dog sitter for my Golden Retriever.";
        String location = "Sliema";
        String imagePath = "/data/user/0/com.example.dogsitmalta/files/images/dog.jpg";
        long createdAt = 1700000000000L;
        long updatedAt = 1700000050000L;

        BlogPost customPost = new BlogPost(id, title, message, location, imagePath, createdAt, updatedAt);

        assertEquals(id, customPost.getId());
        assertEquals(title, customPost.getTitle());
        assertEquals(message, customPost.getMessage());
        assertEquals(location, customPost.getLocation());
        assertEquals(imagePath, customPost.getImagePath());
        assertEquals(createdAt, customPost.getCreatedAt());
        assertEquals(updatedAt, customPost.getUpdatedAt());
    }

    @Test
    public void testSetAndGetId_setsAndReturnsId() {
        post.setId(42L);
        assertEquals(42L, post.getId());
    }

    @Test
    public void testSetAndGetTitle_setsAndReturnsTitle() {
        String title = "Pet Sitting in Valletta";
        post.setTitle(title);
        assertEquals(title, post.getTitle());
    }

    @Test
    public void testSetAndGetMessage_setsAndReturnsMessage() {
        String message = "Available every weekend to take care of small dogs.";
        post.setMessage(message);
        assertEquals(message, post.getMessage());
    }

    @Test
    public void testSetAndGetLocation_setsAndReturnsLocation() {
        String location = "Mdina";
        post.setLocation(location);
        assertEquals(location, post.getLocation());
    }

    @Test
    public void testSetAndGetImagePath_setsAndReturnsImagePath() {
        String imagePath = "/storage/emulated/0/Pictures/dog.png";
        post.setImagePath(imagePath);
        assertEquals(imagePath, post.getImagePath());
    }

    @Test
    public void testSetAndGetCreatedAt_setsAndReturnsCreatedAt() {
        long timestamp = 1680000000000L;
        post.setCreatedAt(timestamp);
        assertEquals(timestamp, post.getCreatedAt());
    }

    @Test
    public void testSetAndGetUpdatedAt_setsAndReturnsUpdatedAt() {
        long timestamp = 1680000100000L;
        post.setUpdatedAt(timestamp);
        assertEquals(timestamp, post.getUpdatedAt());
    }

    @Test
    public void testSetAndGetLatitude_setsAndReturnsLatitude() {
        Double lat = 35.8997;
        post.setLatitude(lat);
        assertEquals(lat, post.getLatitude());
    }

    @Test
    public void testSetAndGetLongitude_setsAndReturnsLongitude() {
        Double lng = 14.5147;
        post.setLongitude(lng);
        assertEquals(lng, post.getLongitude());
    }

    @Test
    public void testSetAndGetRemoteId_setsAndReturnsRemoteId() {
        String remoteId = "550e8400-e29b-41d4-a716-446655440000";
        post.setRemoteId(remoteId);
        assertEquals(remoteId, post.getRemoteId());
    }

    @Test
    public void testSetAndGetOwnerUid_setsAndReturnsOwnerUid() {
        String ownerUid = "firebase_user_abc123";
        post.setOwnerUid(ownerUid);
        assertEquals(ownerUid, post.getOwnerUid());
    }

    @Test
    public void testSetAndGetDeleted_setsAndReturnsDeletedState() {
        post.setDeleted(true);
        assertTrue(post.isDeleted());

        post.setDeleted(false);
        assertFalse(post.isDeleted());
    }

    @Test
    public void testPropertyUpdates_overwritesPreviousValues() {
        post.setTitle("Initial Title");
        post.setMessage("Initial Message");
        post.setUpdatedAt(1000L);

        assertEquals("Initial Title", post.getTitle());

        post.setTitle("Updated Title");
        post.setMessage("Updated Message");
        post.setUpdatedAt(2000L);

        assertEquals("Updated Title", post.getTitle());
        assertEquals("Updated Message", post.getMessage());
        assertEquals(2000L, post.getUpdatedAt());
    }

    @Test
    public void testSetters_acceptNullValuesForNullableFields() {
        post.setTitle("Temp Title");
        post.setMessage("Temp Message");
        post.setLocation("Temp Location");
        post.setImagePath("Temp Path");
        post.setLatitude(35.9);
        post.setLongitude(14.4);
        post.setRemoteId("uuid-123");
        post.setOwnerUid("uid-456");

        post.setTitle(null);
        post.setMessage(null);
        post.setLocation(null);
        post.setImagePath(null);
        post.setLatitude(null);
        post.setLongitude(null);
        post.setRemoteId(null);
        post.setOwnerUid(null);

        assertNull(post.getTitle());
        assertNull(post.getMessage());
        assertNull(post.getLocation());
        assertNull(post.getImagePath());
        assertNull(post.getLatitude());
        assertNull(post.getLongitude());
        assertNull(post.getRemoteId());
        assertNull(post.getOwnerUid());
    }

    @Test
    public void testSetters_acceptEmptyStrings() {
        post.setTitle("");
        post.setMessage("");
        post.setLocation("");
        post.setImagePath("");
        post.setRemoteId("");
        post.setOwnerUid("");

        assertEquals("", post.getTitle());
        assertEquals("", post.getMessage());
        assertEquals("", post.getLocation());
        assertEquals("", post.getImagePath());
        assertEquals("", post.getRemoteId());
        assertEquals("", post.getOwnerUid());
    }

    @Test
    public void testBoundaryValues_handlesExtremeNumbers() {
        post.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, post.getId());

        post.setId(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, post.getId());

        post.setId(-1L);
        assertEquals(-1L, post.getId());

        post.setCreatedAt(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, post.getCreatedAt());

        post.setUpdatedAt(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, post.getUpdatedAt());
    }

    @Test
    public void testLatitudeAndLongitude_handlesExtremeCoordinatesAndNulls() {
        post.setLatitude(-90.0);
        post.setLongitude(-180.0);
        assertEquals(Double.valueOf(-90.0), post.getLatitude());
        assertEquals(Double.valueOf(-180.0), post.getLongitude());

        post.setLatitude(90.0);
        post.setLongitude(180.0);
        assertEquals(Double.valueOf(90.0), post.getLatitude());
        assertEquals(Double.valueOf(180.0), post.getLongitude());

        post.setLatitude(0.0);
        post.setLongitude(0.0);
        assertEquals(Double.valueOf(0.0), post.getLatitude());
        assertEquals(Double.valueOf(0.0), post.getLongitude());
    }
}
