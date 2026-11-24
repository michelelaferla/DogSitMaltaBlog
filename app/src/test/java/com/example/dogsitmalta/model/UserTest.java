package com.example.dogsitmalta.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link User} domain model class.
 * Tests object construction, getter/setter state integrity, boundary values,
 * active status toggles, and null handling.
 */
public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User();
    }

    @Test
    public void testDefaultConstructor_initializesDefaultValues() {
        User defaultUser = new User();
        assertEquals(0L, defaultUser.getUserID());
        assertNull(defaultUser.getUserName());
        assertNull(defaultUser.getUserSurname());
        assertNull(defaultUser.getUserEmail());
        assertNull(defaultUser.getUserPassword());
        assertEquals(0L, defaultUser.getUserDateCreated());
        assertNull(defaultUser.getUserDateUpdated());
        assertTrue("User should default to active", defaultUser.isUserIsActive());
        assertNull(defaultUser.getFirebaseUid());
        assertEquals(0L, defaultUser.getProfileUpdatedAt());
    }

    @Test
    public void testSetAndGetUserID_setsAndReturnsID() {
        user.setUserID(105L);
        assertEquals(105L, user.getUserID());
    }

    @Test
    public void testSetAndGetUserNameAndSurname_setsAndReturnsNames() {
        user.setUserName("Mario");
        user.setUserSurname("Vassallo");

        assertEquals("Mario", user.getUserName());
        assertEquals("Vassallo", user.getUserSurname());
    }

    @Test
    public void testSetAndGetUserEmail_setsAndReturnsEmail() {
        String email = "mario.vassallo@example.com";
        user.setUserEmail(email);
        assertEquals(email, user.getUserEmail());
    }

    @Test
    public void testSetAndGetUserPassword_setsAndReturnsHash() {
        String hash = "210000:salt:hash";
        user.setUserPassword(hash);
        assertEquals(hash, user.getUserPassword());
    }

    @Test
    public void testSetAndGetDates_setsAndReturnsTimestamps() {
        long created = 1670000000000L;
        Long updated = 1680000000000L;
        long profileUpdated = 1690000000000L;

        user.setUserDateCreated(created);
        user.setUserDateUpdated(updated);
        user.setProfileUpdatedAt(profileUpdated);

        assertEquals(created, user.getUserDateCreated());
        assertEquals(updated, user.getUserDateUpdated());
        assertEquals(profileUpdated, user.getProfileUpdatedAt());
    }

    @Test
    public void testSetAndGetUserIsActive_togglesActiveState() {
        user.setUserIsActive(false);
        assertFalse(user.isUserIsActive());

        user.setUserIsActive(true);
        assertTrue(user.isUserIsActive());
    }

    @Test
    public void testSetAndGetFirebaseUid_setsAndReturnsUid() {
        String uid = "firebase_user_uid_123";
        user.setFirebaseUid(uid);
        assertEquals(uid, user.getFirebaseUid());
    }

    @Test
    public void testSetters_acceptNullValuesForNullableFields() {
        user.setUserName("Name");
        user.setUserSurname("Surname");
        user.setUserEmail("email@example.com");
        user.setUserPassword("hash");
        user.setFirebaseUid("uid");
        user.setUserDateUpdated(1000L);

        user.setUserName(null);
        user.setUserSurname(null);
        user.setUserEmail(null);
        user.setUserPassword(null);
        user.setFirebaseUid(null);
        user.setUserDateUpdated(null);

        assertNull(user.getUserName());
        assertNull(user.getUserSurname());
        assertNull(user.getUserEmail());
        assertNull(user.getUserPassword());
        assertNull(user.getFirebaseUid());
        assertNull(user.getUserDateUpdated());
    }

    @Test
    public void testBoundaryValues_handlesExtremeIDsAndTimestamps() {
        user.setUserID(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, user.getUserID());

        user.setUserDateCreated(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, user.getUserDateCreated());

        user.setUserDateUpdated(Long.MAX_VALUE);
        assertEquals(Long.valueOf(Long.MAX_VALUE), user.getUserDateUpdated());
    }
}
