package com.zipeg.gae;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;

import static com.zipeg.gae.util.*;

public class crypto {

    // to be on the safe side not even thread local cache:
    // http://www.fitc.unc.edu.ar/javadev/crypto/cryptobugs.html

    public static Mac getMac(String m) {
        try {
            timestamp("Mac.getInstance(" + m + ")");
            Mac mac = Mac.getInstance(m);
            timestamp("Mac.getInstance(" + m + ")");
            return mac;
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    public static Cipher getCipher(String c) {
        try {
            return Cipher.getInstance(c); // ~35microseconds
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        } catch (NoSuchPaddingException e) {
            throw new Error(e);
        }
    }

    public static byte[] encrypt(byte[] data, byte[] key) { // ~100 microseconds
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = getCipher("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new Error(e);
        } catch (BadPaddingException e) {
            throw new Error(e);
        } catch (InvalidKeyException e) {
            throw new Error(e);
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key) { // ~100 microseconds
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = getCipher("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new Error(e);
        } catch (BadPaddingException e) {
            throw new Error(e);
        } catch (InvalidKeyException e) {
            throw new Error(e);
        }
    }

    public static String encrypt(String data, String hexKey) {
        return str.toHex(encrypt(str.toUTF8(data), str.fromHex(hexKey)));
    }

    public static String decrypt(String hexData, String hexKey) {
        return str.fromUTF8(decrypt(str.fromHex(hexData), str.fromHex(hexKey)));
    }

    public static String encryptJson(Map<Object, Object> data, String hexKey) {
        return encrypt(json.encode(data), hexKey); // ~398 microseconds
    }

    public static Map<Object, Object> decryptJson(String hexData, String hexKey) {
        return json.decode(decrypt(hexData, hexKey)); // ~429 microseconds
    }

    public static String encryptJson(Map<Object, Object> data) {
        return encrypt(json.encode(data), Context.get().server.get("aes_key"));
    }

    public static Map<Object, Object> decryptJson(String hexData) {
        return json.decode(decrypt(hexData, Context.get().server.get("aes_key")));
    }

    public static void main(String a[]) throws NoSuchAlgorithmException {
        timestamp("KeyGenerator.getInstance");
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        timestamp("KeyGenerator.getInstance");
        gen.init(256);
        SecretKey sk = gen.generateKey();
        String key = str.toHex(sk.getEncoded());
        System.out.println("AES256_KEY=" + key);
        String data = "Big brown fox jumped over the lazy dog";
        for (int i = 0; i < 10; i++) {
            timestamp("encrypt");
            String encrypted = encrypt(data, key);
            timestamp("encrypt");
            timestamp("decrypt");
            String decrypted = decrypt(encrypted, key);
            timestamp("decrypt");
            System.out.println("data=" + data);
            assert decrypted.equals(data);
            Map<Object, Object> map1 = new HashMap<Object, Object>();
            map1.put("key1", data);
            map1.put("key2", data);
            map1.put("key3", data);
            map1.put("key4", data);
            timestamp("encryptJson");
            encrypted = encryptJson(map1, key);
            timestamp("encryptJson");
            timestamp("decryptJson");
            Map<Object, Object> map2 = decryptJson(encrypted, key);
            timestamp("decryptJson");
            assert map1.equals(map2);
        }
    }

}
