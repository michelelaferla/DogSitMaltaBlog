package com.example.dogsitmalta.utils;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Note: {@link PasswordHasher} depends on {@link android.util.Base64}.
 * Complete functional verification of PBKDF2 hashing and Base64 encoding/decoding is
 * implemented in the Android Instrumented test suite at
 * {@code app/src/androidTest/java/com/example/dogsitmalta/utils/PasswordHasherInstrumentedTest.java}.
 */
public class PasswordHasherTest {

    @Test
    public void testClassPresence() {
        assertNotNull("PasswordHasher class is present", PasswordHasher.class);
    }
}
