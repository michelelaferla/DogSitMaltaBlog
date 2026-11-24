package com.example.dogsitmalta.database.entities;

import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.model.User;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PostEntity} and {@link UserEntity} model-to-entity mapping.
 * Verifies two-way conversion accuracy between Room database entities and UI domain models.
 */
public class EntityConversionTest {

    @Test
    public void testPostEntityConversion_modelToEntityAndBackPreservesData() {
        BlogPost model = new BlogPost();
        model.setId(50L);
        model.setTitle("Conversion Test Title");
        model.setMessage("Conversion Test Message");
        model.setLocation("Conversion Location");
        model.setImagePath("/data/images/test.jpg");
        model.setCreatedAt(1700000000000L);
        model.setUpdatedAt(1700000050000L);
        model.setLatitude(35.9);
        model.setLongitude(14.5);
        model.setRemoteId("uuid-convert-123");
        model.setOwnerUid("uid-owner-456");
        model.setDeleted(true);

        // Model -> Entity
        PostEntity entity = PostEntity.from(model);
        assertEquals(50L, entity.id);
        assertEquals("Conversion Test Title", entity.title);
        assertEquals("Conversion Test Message", entity.message);
        assertEquals("Conversion Location", entity.location);
        assertEquals("/data/images/test.jpg", entity.imagePath);
        assertEquals(1700000000000L, entity.createdAt);
        assertEquals(1700000050000L, entity.updatedAt);
        assertEquals(Double.valueOf(35.9), entity.latitude);
        assertEquals(Double.valueOf(14.5), entity.longitude);
        assertEquals("uuid-convert-123", entity.remoteId);
        assertEquals("uid-owner-456", entity.ownerUid);
        assertTrue(entity.deleted);

        // Entity -> Model
        BlogPost convertedModel = entity.toModel();
        assertEquals(50L, convertedModel.getId());
        assertEquals("Conversion Test Title", convertedModel.getTitle());
        assertEquals("Conversion Test Message", convertedModel.getMessage());
        assertEquals("Conversion Location", convertedModel.getLocation());
        assertEquals("/data/images/test.jpg", convertedModel.getImagePath());
        assertEquals(1700000000000L, convertedModel.getCreatedAt());
        assertEquals(1700000050000L, convertedModel.getUpdatedAt());
        assertEquals(Double.valueOf(35.9), convertedModel.getLatitude());
        assertEquals(Double.valueOf(14.5), convertedModel.getLongitude());
        assertEquals("uuid-convert-123", convertedModel.getRemoteId());
        assertEquals("uid-owner-456", convertedModel.getOwnerUid());
        assertTrue(convertedModel.isDeleted());
    }

    @Test
    public void testUserEntityConversion_modelToEntityAndBackPreservesData() {
        User userModel = new User();
        userModel.setUserID(99L);
        userModel.setUserName("Sarah");
        userModel.setUserSurname("Borg");
        userModel.setUserEmail("sarah.borg@example.com");
        userModel.setUserPassword("210000:salt:hash");
        userModel.setUserDateCreated(1600000000000L);
        userModel.setUserDateUpdated(1650000000000L);
        userModel.setUserIsActive(false);
        userModel.setFirebaseUid("fb_uid_sarah");
        userModel.setProfileUpdatedAt(1680000000000L);

        // Model -> Entity
        UserEntity userEntity = UserEntity.from(userModel);
        assertEquals(99L, userEntity.id);
        assertEquals("Sarah", userEntity.name);
        assertEquals("Borg", userEntity.surname);
        assertEquals("sarah.borg@example.com", userEntity.email);
        assertEquals("210000:salt:hash", userEntity.passwordHash);
        assertEquals(1600000000000L, userEntity.createdAt);
        assertEquals(Long.valueOf(1650000000000L), userEntity.passwordUpdatedAt);
        assertFalse(userEntity.active);
        assertEquals("fb_uid_sarah", userEntity.firebaseUid);
        assertEquals(1680000000000L, userEntity.profileUpdatedAt);

        // Entity -> Model
        User convertedUser = userEntity.toModel();
        assertEquals(99L, convertedUser.getUserID());
        assertEquals("Sarah", convertedUser.getUserName());
        assertEquals("Borg", convertedUser.getUserSurname());
        assertEquals("sarah.borg@example.com", convertedUser.getUserEmail());
        assertEquals("210000:salt:hash", convertedUser.getUserPassword());
        assertEquals(1600000000000L, convertedUser.getUserDateCreated());
        assertEquals(Long.valueOf(1650000000000L), convertedUser.getUserDateUpdated());
        assertFalse(convertedUser.isUserIsActive());
        assertEquals("fb_uid_sarah", convertedUser.getFirebaseUid());
        assertEquals(1680000000000L, convertedUser.getProfileUpdatedAt());
    }
}
