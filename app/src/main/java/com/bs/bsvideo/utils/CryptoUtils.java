package com.bs.bsvideo.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.security.SecureRandom;

public class CryptoUtils {
//
//    public static final int GCM_IV_LENGTH = 12;
//    public static final int GCM_TAG_LENGTH = 128;
//
//    public static SecretKey deriveKeyFromPassword(char[] password, byte[] salt) throws Exception {
//        int iterations = 200_000;
//        int keyLength = 256;
//        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
//        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
//        return new SecretKeySpec(keyBytes, "AES");
//    }
//
//    public static void encryptFileWithKey(SecretKey key, InputStream is, OutputStream os) throws Exception {
//        byte[] iv = new byte[GCM_IV_LENGTH];
//        new SecureRandom().nextBytes(iv);
//
//        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
//        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
//
//        try (CipherOutputStream cos = new CipherOutputStream(os, cipher)) {
//
//            os.write(iv);
//
//            byte[] buffer = new byte[4096];
//            int read;
//            while ((read = is.read(buffer)) != -1) {
//                cos.write(buffer, 0, read);
//            }
//            cos.flush();
//        }
//    }
//
//    public static void decryptFileWithKey(SecretKey key, InputStream is, OutputStream os) throws Exception {
//        byte[] iv = new byte[GCM_IV_LENGTH];
//        if (is.read(iv) != GCM_IV_LENGTH) {
//            throw new IllegalArgumentException("Invalid input file - no IV");
//        }
//
//        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
//        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//        cipher.init(Cipher.DECRYPT_MODE, key, spec);
//
//        byte[] buffer = new byte[4096];
//        int read;
//        while ((read = is.read(buffer)) != -1) {
//            byte[] decrypted = cipher.update(buffer);
//            os.write(decrypted, 0, read);
//        }
//        os.flush();
//    }



    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 128;

    // --- Key derivation ---
    public static SecretKey deriveKeyFromPassword(char[] password, byte[] salt) throws Exception {
        int iterations = 200_000;
        int keyLength = 256;
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    // --- Encrypt: manual chunk mode ---
    public static void encryptFileWithKey(SecretKey key, InputStream is, OutputStream os) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        os.write(iv);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec spec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            byte[] encrypted = cipher.update(buffer, 0, read);
            if (encrypted != null) {
                os.write(encrypted);
            }
        }

        byte[] finalBytes = cipher.doFinal();
        if (finalBytes != null) os.write(finalBytes);
        os.flush();
    }

    public static void decryptFileWithKey(SecretKey key, InputStream is, OutputStream os) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        if (is.read(iv) != GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Invalid input - no IV");
        }

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec spec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            byte[] decrypted = cipher.update(buffer, 0, read);
            if (decrypted != null) {
                os.write(decrypted);
            }
        }

        byte[] finalBytes = cipher.doFinal();
        if (finalBytes != null) {
            os.write(finalBytes);
        }
        os.close();
    }

}
