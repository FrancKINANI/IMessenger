package i.imessenger.utils;

import android.util.Base64;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    private static final String ALGORITHM = "AES";
    // In a production app, this key should be securely stored in Android Keystore, not hardcoded.
    // Length must be 16, 24, or 32 bytes for AES-128, AES-192, or AES-256.
    /*
    ENCRYPTION KEY:79a0a31d9a0e9275020bae805d67800fa4a3bcb7de5275edf01858eb78f7f074
    INITIALIZATION VECTOR (IV):b6d66a0dd6c04d9872dfb0c1e42efc45
    Algorithm:AES
    Key Size:256 bits
    Format:hex
    Strength:MAXIMUM
    */

    private static final byte[] KEY = "79a0a31d9a0e9275020bae805d67800fa4a3bcb7de5275edf01858eb78f7f074".getBytes(); 

    public static String encrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        return Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
    }

    public static String decrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(value, Base64.DEFAULT);
        byte[] decryptedValue = cipher.doFinal(decodedValue);
        return new String(decryptedValue, "utf-8");
    }

    private static Key generateKey() throws Exception {
        return new SecretKeySpec(KEY, ALGORITHM);
    }
}
