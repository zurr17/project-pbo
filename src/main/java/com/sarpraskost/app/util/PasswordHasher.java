package com.sarpraskost.app.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility untuk hashing dan verifikasi password menggunakan PBKDF2WithHmacSHA256.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password tidak boleh null.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = pbkdf2(rawPassword.toCharArray(), salt);
        HexFormat hex = HexFormat.of();
        return hex.formatHex(salt) + ":" + hex.formatHex(derived);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        int sep = storedHash.indexOf(':');
        if (sep <= 0 || sep >= storedHash.length() - 1) {
            return MessageDigest.isEqual(
                rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
        }
        try {
            HexFormat hex = HexFormat.of();
            byte[] salt = hex.parseHex(storedHash.substring(0, sep));
            byte[] expected = hex.parseHex(storedHash.substring(sep + 1));
            byte[] actual = pbkdf2(rawPassword.toCharArray(), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] chars, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Gagal memproses hash kata sandi.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
