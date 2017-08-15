package com.books.dmitriy.reader.utils;

import android.util.Base64;

import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;


public class EncodeDecode {

    public ArrayList <String> encode (String htmltext) {
        ArrayList<String> result = new ArrayList();
        try {
        // Зашифровка текста.
        SecretKeySpec sks = null;
        byte[] key = null;

        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed("some key".getBytes());
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128, sr);
        key = kg.generateKey().getEncoded();
        sks = new SecretKeySpec(key, "AES");
        byte[] encodedBytes = null;
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, sks);
        encodedBytes = c.doFinal(htmltext.getBytes());

        result.add(Base64.encodeToString(key, Base64.DEFAULT));
        result.add(Base64.encodeToString(encodedBytes, Base64.DEFAULT));

        } catch (Exception e) {
            result.add("empty");
            result.add("empty");
        }
        return result;
    }

public String decode (String keytext, String text) {
    String decodedtext="empty";
    // Расшифровка текста.
    byte[] key = Base64.decode(keytext, Base64.DEFAULT);
    byte[] strBytes = Base64.decode(text, Base64.DEFAULT);
    SecretKeySpec sks = new SecretKeySpec(key, "AES");
    byte[] decodedBytes = null;
    try {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, sks);
        decodedBytes = c.doFinal(strBytes);
        decodedtext = new String(decodedBytes);
    } catch (Exception e) {
    }
    return decodedtext;
}

}
