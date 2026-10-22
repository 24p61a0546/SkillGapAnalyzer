package com.skillgap.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Utility for hashing and verifying passwords securely using PBKDF2 with HMAC-SHA256.
 * Complies with OWASP guidelines using cryptographic random salting (128-bit),
 * 65,536 iterations, and constant-time equality comparisons to prevent timing attacks.
 * Also supports transparent fallback verification for legacy SHA-256 hashes.
 */
public class PasswordUtil {

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 65536;
    private static final int SALT_BYTES = 16;       // 128-bit salt
    private static final int HASH_BITS = 256;       // 256-bit derived key (32 bytes)
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Hashes a plaintext password using PBKDF2WithHmacSHA256 with a unique random salt.
     * Stored format: <iterations>:<saltHex>:<hashHex>
     *
     * @param plainPassword Plaintext password string
     * @return Formatted hash string containing iterations, salt, and key
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, DEFAULT_ITERATIONS, HASH_BITS);
        return DEFAULT_ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Verifies a candidate plaintext password against a stored hash string.
     *
     * @param plainPassword Candidate plaintext password
     * @param storedHash    Stored hash (<iterations>:<salt>:<hash> or legacy 64-char SHA256)
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.trim().isEmpty()) {
            return false;
        }

        String[] parts = storedHash.split(":");
        if (parts.length == 3) {
            try {
                int iterations = Integer.parseInt(parts[0]);
                byte[] salt = fromHex(parts[1]);
                byte[] expectedHash = fromHex(parts[2]);
                byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt, iterations, expectedHash.length * 8);

                // Constant-time comparison to prevent timing side-channel attacks
                return MessageDigest.isEqual(expectedHash, actualHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Fallback for legacy plain SHA-256 hashes (64 hex characters)
        if (storedHash.length() == 64 && !storedHash.contains(":")) {
            return verifyLegacySha256(plainPassword, storedHash);
        }

        return false;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 algorithm unavailable on current JVM", e);
        }
    }

    private static boolean verifyLegacySha256(String plainPassword, String storedHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(fromHex(storedHash), encodedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
