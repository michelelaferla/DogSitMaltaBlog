package com.example.dogsitmalta.utils;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Education demo: PBKDF2 password verifier for offline accounts. Prefer Firebase Auth online. */
public final class PasswordHasher {
    private static final int ITERATIONS = 210000;
    private PasswordHasher() { }
    public static String hash(char[] password) {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return ITERATIONS + ":" + Base64.encodeToString(salt, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(derive(password, salt, ITERATIONS), Base64.NO_WRAP);
    }
    public static boolean verify(char[] password, String encoded) {
        if (encoded == null) return false;
        try {
            String[] parts = encoded.split(":");
            if (parts.length != 3) return false;
            int iterations = Integer.parseInt(parts[0]);
            if (iterations < 100000 || iterations > 1000000) return false;
            byte[] salt = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] expected = Base64.decode(parts[2], Base64.NO_WRAP);
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException exception) { return false; }
    }
    private static byte[] derive(char[] pass, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(pass, salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded(); }
        catch (Exception exception) { throw new IllegalStateException("Password hashing failed", exception); }
        finally { spec.clearPassword(); }
    }
}
