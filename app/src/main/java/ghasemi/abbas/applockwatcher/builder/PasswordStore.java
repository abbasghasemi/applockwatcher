package ghasemi.abbas.applockwatcher.builder;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Stores a salted PIN, pattern or password hash and migrates existing plain-text values. */
public final class PasswordStore {
    private static final int ITERATIONS = 150000;
    private static final int HASH_BITS = 256;

    private PasswordStore() { }

    public static void save(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(password, salt);
        TinyData.getInstance().putPasswordHash(Base64.encodeToString(salt, Base64.NO_WRAP),
                Base64.encodeToString(hash, Base64.NO_WRAP), password.length());
    }

    public static boolean matches(String candidate) {
        TinyData data = TinyData.getInstance();
        String encodedHash = data.getString("passwordHash");
        if (!encodedHash.isEmpty()) {
            if (!String.valueOf(candidate.length()).equals(data.getString("passwordLength"))) return false;
            try {
                byte[] salt = Base64.decode(data.getString("passwordSalt"), Base64.NO_WRAP);
                byte[] expected = Base64.decode(encodedHash, Base64.NO_WRAP);
                return MessageDigest.isEqual(expected, derive(candidate, salt));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        String legacy = data.getString("password");
        if (legacy.isEmpty()) return false;
        boolean matched = MessageDigest.isEqual(candidate.getBytes(StandardCharsets.UTF_8),
                legacy.getBytes(StandardCharsets.UTF_8));
        if (matched) save(candidate);
        return matched;
    }

    private static byte[] derive(String value, byte[] salt) {
        char[] chars = value.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        } finally {
            spec.clearPassword();
            Arrays.fill(chars, '\0');
        }
    }
}
