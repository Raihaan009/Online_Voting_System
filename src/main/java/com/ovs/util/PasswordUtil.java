package com.ovs.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Security utility for cryptographic hashing and verification of passwords
 * using the adaptive Blowfish-based BCrypt algorithm.
 */
public final class PasswordUtil {

    /**
     * BCrypt cost factor / workload log rounds (2^12 = 4,096 iterations).
     * Provides strong defense against hardware brute-force while keeping response times fast (<250ms).
     */
    private static final int BCRYPT_LOG_ROUNDS = 12;

    /**
     * Private constructor to prevent direct instantiation of utility class.
     */
    private PasswordUtil() {
        throw new UnsupportedOperationException("PasswordUtil is a utility class and cannot be instantiated");
    }

    /**
     * Hashes a plaintext password using BCrypt with a secure 128-bit salt and 12 log rounds.
     *
     * @param plainPassword the plaintext password to hash
     * @return the 60-character BCrypt hashed password string
     * @throws IllegalArgumentException if the provided plainPassword is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password to hash cannot be null or empty.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verifies a plaintext password candidate against an existing BCrypt hashed password.
     * Implements complete null safety and defensive handling of malformed hashes.
     *
     * @param plainPassword  the raw plaintext password candidate entered by the user
     * @param hashedPassword the stored BCrypt hash from the database
     * @return true if the password matches the hash, false if invalid or if inputs are null/empty
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty() ||
            hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Catches malformed or unsupported hash formats gracefully without leaking exceptions
            return false;
        }
    }
}
