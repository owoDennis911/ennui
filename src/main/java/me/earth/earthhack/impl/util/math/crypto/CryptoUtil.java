package me.earth.earthhack.impl.util.math.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utility class for AES/GCM encryption and decryption with PBKDF2 key derivation,
 * salt, IV, and optional AAD support. Encrypted payloads are Base64-encoded
 * and shuffled to obscure their structure.
 * Blob layout (before shuffle):
 * 1 byte version
 * 16 bytes salt
 * 12 bytes IV
 * ciphertext + 16-byte GCM tag
 */
public class CryptoUtil {

    private static final byte   VERSION    = 1;
    private static final String PASSPHRASE = "cesar code 2.0";
    private static final String PEPPER     = System.getenv("WEBHOOK_PEPPER");
    private static final int    SALT_LEN   = 16;
    private static final int    IV_LEN     = 12;
    private static final int    TAG_LEN    = 128;
    private static final int    ITERATIONS = 100_000;

    /**
     * Encrypts the given text using AES/GCM/NoPadding.
     *
     * @param plainText the plaintext string to encrypt
     * @return a shuffled Base64-encoded blob, or null if any error occurs
     */
    public static String encrypt(String plainText) {
        try {
            SecureRandom random = new SecureRandom();

            byte[] salt = new byte[SALT_LEN];
            random.nextBytes(salt);

            SecretKeySpec key = deriveKey(PASSPHRASE + PEPPER, salt);

            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LEN, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);


            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buf = ByteBuffer.allocate(1 + SALT_LEN + IV_LEN + cipherText.length);
            buf.put(VERSION).put(salt).put(iv).put(cipherText);

            String b64 = Base64.getEncoder().encodeToString(buf.array());
            return shuffle(b64);

        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Decrypts a shuffled Base64-encoded blob produced by {@link #encrypt}.
     *
     * @param shuffledBlob the shuffled Base64 string
     * @return the decrypted plaintext, or null if version mismatch or any error occurs
     */
    public static String decrypt(String shuffledBlob) {
        try {
            String b64 = deshuffle(shuffledBlob);
            byte[] all = Base64.getDecoder().decode(b64);
            ByteBuffer buf = ByteBuffer.wrap(all);

            byte version = buf.get();
            if (version != VERSION) {
                return null;
            }

            byte[] salt = new byte[SALT_LEN];
            buf.get(salt);

            byte[] iv = new byte[IV_LEN];
            buf.get(iv);

            byte[] cipherText = new byte[buf.remaining()];
            buf.get(cipherText);

            SecretKeySpec key = deriveKey(PASSPHRASE + PEPPER, salt);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);

        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Derives an AES key from the given passphrase and salt using PBKDF2WithHmacSHA256.
     *
     * @param pass the passphrase (including pepper)
     * @param salt the random salt
     * @return a 256-bit AES key spec
     * @throws Exception if the key factory fails
     */
    private static SecretKeySpec deriveKey(String pass, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, ITERATIONS, 256);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Performs a reversible shuffle putting the first three characters at the end and the end character at the beginning
     * to obscure the input. Works with strings of any length.
     *
     * @param input the Base64 string to shuffle
     * @return the shuffled string
     */
    public static String shuffle(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder sb = new StringBuilder(input);
        int len = sb.length();

        if (len == 1) {
            return input;
        } else if (len == 2) {
            char temp = sb.charAt(0);
            sb.setCharAt(0, sb.charAt(1));
            sb.setCharAt(1, temp);
        } else if (len == 3) {
            char last = sb.charAt(len - 1);
            sb.deleteCharAt(len - 1);
            sb.insert(0, last);
        } else {
            char last = sb.charAt(len - 1);
            String firstThree = sb.substring(0, 3);
            sb.deleteCharAt(len - 1);
            sb.delete(0, 3);
            sb.insert(0, last);
            sb.append(firstThree);
        }

        return sb.toString();
    }

    /**
     * Reverses the shuffle performed by {@link #shuffle}.
     *
     * @param input the shuffled string
     * @return the original Base64 string
     */
    public static String deshuffle(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder sb = new StringBuilder(input);
        int len = sb.length();

        if (len == 1) {
            return input;
        } else if (len == 2) {
            char temp = sb.charAt(0);
            sb.setCharAt(0, sb.charAt(1));
            sb.setCharAt(1, temp);
        } else if (len == 3) {
            char first = sb.charAt(0);
            sb.deleteCharAt(0);
            sb.append(first);
        } else {
            char first = sb.charAt(0);
            String lastThree = sb.substring(len - 3);
            sb.deleteCharAt(0);
            sb.delete(len - 4, len - 1);
            sb.insert(0, lastThree);
            sb.append(first);
        }

        return sb.toString();
    }
}
