package com.apiblast.customcode.example.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256 {
    public static String hash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(bytes);
        return convertByteArrayToHexString(hashedBytes);
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}
