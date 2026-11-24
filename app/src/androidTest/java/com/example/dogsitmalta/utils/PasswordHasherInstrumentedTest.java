package com.example.dogsitmalta.utils;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented Unit Tests for {@link PasswordHasher}.
 * Since {@link PasswordHasher} relies on {@link android.util.Base64}, these tests
 * execute on the Android runtime environment to verify PBKDF2 hash generation
 * and verifier logic.
 */
@RunWith(AndroidJUnit4.class)
public class PasswordHasherInstrumentedTest {

    @Test
    public void testHash_generatesValidFormattedString() {
        char[] password = "SecurePassword123!".toCharArray();
        String hash = PasswordHasher.hash(password);

        assertNotNull(hash);
        assertTrue("Hash string should start with iteration count 210000", hash.startsWith("210000:"));
        String[] parts = hash.split(":");
        assertEquals("Hash format must contain 3 colon-separated parts", 3, parts.length);
    }

    @Test
    public void testVerify_returnsTrueForCorrectPassword() {
        char[] password = "MySecretPassword123".toCharArray();
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify("MySecretPassword123".toCharArray(), hash);
        assertTrue("Correct password verification must succeed", result);
    }

    @Test
    public void testVerify_returnsFalseForIncorrectPassword() {
        char[] password = "MySecretPassword123".toCharArray();
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify("WrongPassword456".toCharArray(), hash);
        assertFalse("Incorrect password verification must fail", result);
    }

    @Test
    public void testHash_differentPasswordsProduceDifferentHashes() {
        String hash1 = PasswordHasher.hash("PasswordOne".toCharArray());
        String hash2 = PasswordHasher.hash("PasswordTwo".toCharArray());

        assertNotEquals("Different passwords must produce different hashes", hash1, hash2);
    }

    @Test
    public void testHash_samePasswordProducesUniqueHashesDueToRandomSalt() {
        char[] password = "IdenticalPassword".toCharArray();
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);

        assertNotEquals("Random salt generation must ensure unique hashes for identical passwords", hash1, hash2);
    }

    @Test
    public void testVerify_returnsFalseForNullOrMalformedInput() {
        char[] pass = "password".toCharArray();

        assertFalse("Null hash string should return false", PasswordHasher.verify(pass, null));
        assertFalse("Empty hash string should return false", PasswordHasher.verify(pass, ""));
        assertFalse("Single-part string should return false", PasswordHasher.verify(pass, "invalidhash"));
        assertFalse("Two-part string should return false", PasswordHasher.verify(pass, "210000:salt"));
        assertFalse("Malformed base64 parts should return false", PasswordHasher.verify(pass, "210000:not_base64!!!:not_base64!!!"));
    }

    @Test
    public void testVerify_returnsFalseForOutOfRangeIterationCount() {
        char[] pass = "password".toCharArray();

        // Iteration count too low (< 100,000)
        assertFalse("Iteration count below 100,000 must fail validation",
                PasswordHasher.verify(pass, "50000:salt:hash"));

        // Iteration count too high (> 1,000,000)
        assertFalse("Iteration count above 1,000,000 must fail validation",
                PasswordHasher.verify(pass, "2000000:salt:hash"));
    }
}
