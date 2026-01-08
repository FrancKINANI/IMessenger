package i.imessenger.utils;

import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_HEX = "79a0a31d9a0e9275020bae805d67800fa4a3bcb7de5275edf01858eb78f7f074";
    private static final byte[] KEY = hexStringToByteArray(KEY_HEX);
    private static final int GCM_IV_LENGTH = 12; // 12 bytes IV for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bit auth tag

    public static String encrypt(String value) throws Exception {
        if (value == null)
            return null;

        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), spec);

        byte[] encryptedBytes = cipher.doFinal(value.getBytes("UTF-8"));

        // Prepend IV to ciphertext
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedBytes);

        return Base64.encodeToString(byteBuffer.array(), Base64.DEFAULT);
    }

    public static String decrypt(String value) throws Exception {
        if (value == null)
            return null;

        byte[] decodedValue = Base64.decode(value, Base64.DEFAULT);

        // Extract IV
        if (decodedValue.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted data length");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedValue);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        byte[] encryptedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedBytes);

        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), spec);

        byte[] decryptedValue = cipher.doFinal(encryptedBytes);
        return new String(decryptedValue, "UTF-8");
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
