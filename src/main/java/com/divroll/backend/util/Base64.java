/*
Copyright (c) 2009, Mark Renouf
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Mark Renouf ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.divroll.backend.util;
/**
 * Custom Base64 encode/decode implementation suitable for use in
 * GWT applications (uses only translatable classes). 
 */
public class Base64 {

    private static final String etab =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    private static byte[] dtab = {
        -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
        -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
        -1,-1,-1,-1,-1,-1,-1,-1,-1,62,-1,-1,-1,63,52,53,54,
        55,56,57,58,59,60,61,-1,-1,-1,64,-1,-1,-1, 0, 1, 2,
         3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18,19,
        20,21,22,23,24,25,-1,-1,-1,-1,-1,-1,26,27,28,29,30,
        31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,
        48,49,50,51,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    };

    public static String decode(String data) {
        StringBuffer out = new StringBuffer();
        
        // length must be multiple of 4 (with padding)
        if (data.length() % 4 != 0)
            return "";

        for (int i = 0; i < data.length();) {
            byte e0 = dtab[data.charAt(i++) & 0x7f];
            byte e1 = dtab[data.charAt(i++) & 0x7f];
            byte e2 = dtab[data.charAt(i++) & 0x7f];
            byte e3 = dtab[data.charAt(i++) & 0x7f];
            
            // Invalid characters in input
            if (e0 == -1 || e1 == -1 || e2 == -1 || e3 == -1)
                return "";
            
            byte d0 = (byte) ((e0 << 2) + ((e1 >>> 4) & 0x03));
            byte d1 = (byte) ((e1 << 4) + ((e2 >>> 2) & 0x0f));
            byte d2 = (byte) ((e2 << 6) + (e3 & 0x3f));
            
            out.append(Character.toString((char) d0));
            if (e2 != 64)
                out.append(Character.toString((char) d1));
            if (e3 != 64)
                out.append(Character.toString((char) d2));
        }
        return out.toString();
    }
    
    public static String encode(String data) {
        StringBuffer out = new StringBuffer();
        
        int i = 0;
        int r = data.length();
        while (r > 0) {
            byte d0, d1, d2;
            byte e0, e1, e2, e3;
            
            d0 = (byte) data.charAt(i++); --r;
            e0 = (byte) (d0 >>> 2);
            e1 = (byte) ((d0 & 0x03) << 4);
            
            if (r > 0) {
                d1 = (byte) data.charAt(i++); --r;
                e1 += (byte) (d1 >>> 4);
                e2 = (byte) ((d1 & 0x0f) << 2);
            }
            else {
                e2 = 64;
            }
            
            if (r > 0) {
                d2 = (byte) data.charAt(i++); --r;
                e2 += (byte) (d2 >>> 6);
                e3 = (byte) (d2 & 0x3f);
            }
            else {
                e3 = 64;
            }
            out.append(etab.charAt(e0));
            out.append(etab.charAt(e1));
            out.append(etab.charAt(e2));
            out.append(etab.charAt(e3));
        }

        return out.toString();
    }

    /** Added method **/
    /**
     * Translates the specified byte array into a Base64 string as per
     * Preferences.put(byte[]).
     */
    public static String byteArrayToBase64(byte[] a) {
        return byteArrayToBase64(a, false);
    }

    /**
     * Translates the specified byte array into an "alternate representation"
     * Base64 string.  This non-standard variant uses an alphabet that does
     * not contain the uppercase alphabetic characters, which makes it
     * suitable for use in situations where case-folding occurs.
     */
    public static String byteArrayToAltBase64(byte[] a) {
        return byteArrayToBase64(a, true);
    }

    private static String byteArrayToBase64(byte[] a, boolean alternate) {
        int aLen = a.length;
        int numFullGroups = aLen/3;
        int numBytesInPartialGroup = aLen - 3*numFullGroups;
        int resultLen = 4*((aLen + 2)/3);
        StringBuffer result = new StringBuffer(resultLen);
        char[] intToAlpha = (alternate ? intToAltBase64 : intToBase64);

        // Translate all full groups from byte array elements to Base64
        int inCursor = 0;
        for (int i=0; i<numFullGroups; i++) {
            int byte0 = a[inCursor++] & 0xff;
            int byte1 = a[inCursor++] & 0xff;
            int byte2 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            result.append(intToAlpha[(byte0 << 4)&0x3f | (byte1 >> 4)]);
            result.append(intToAlpha[(byte1 << 2)&0x3f | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 0x3f]);
        }

        // Translate partial group if present
        if (numBytesInPartialGroup != 0) {
            int byte0 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(byte0 << 4) & 0x3f]);
                result.append("==");
            } else {
                // assert numBytesInPartialGroup == 2;
                int byte1 = a[inCursor++] & 0xff;
                result.append(intToAlpha[(byte0 << 4)&0x3f | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2)&0x3f]);
                result.append('=');
            }
        }
        // assert inCursor == a.length;
        // assert result.length() == resultLen;
        return result.toString();
    }

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified
     * in Table 1 of RFC 2045.
     */
    private static final char intToBase64[] = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Alternate Base64 Alphabet" equivalents.
     * This is NOT the real Base64 Alphabet as per in Table 1 of RFC 2045.
     * This alternate alphabet does not use the capital letters.  It is
     * designed for use in environments where "case folding" occurs.
     */
    private static final char intToAltBase64[] = {
            '!', '"', '#', '$', '%', '&', '\'', '(', ')', ',', '-', '.', ':',
            ';', '<', '>', '@', '[', ']', '^',  '`', '_', '{', '|', '}', '~',
            'a', 'b', 'c', 'd', 'e', 'f', 'g',  'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't',  'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6',  '7', '8', '9', '+', '?'
    };

    /**
     * Translates the specified Base64 string (as per Preferences.get(byte[]))
     * into a byte array.
     *
     * @throw IllegalArgumentException if <tt>s</tt> is not a valid Base64
     *        string.
     */
    public static byte[] base64ToByteArray(String s) {
        return base64ToByteArray(s, false);
    }

    /**
     * Translates the specified "alternate representation" Base64 string
     * into a byte array.
     *
     * @throw IllegalArgumentException or ArrayOutOfBoundsException
     *        if <tt>s</tt> is not a valid alternate representation
     *        Base64 string.
     */
    public static byte[] altBase64ToByteArray(String s) {
        return base64ToByteArray(s, true);
    }

    private static byte[] base64ToByteArray(String s, boolean alternate) {
        byte[] alphaToInt = (alternate ?  altBase64ToInt : base64ToInt);
        int sLen = s.length();
        int numGroups = sLen/4;
        if (4*numGroups != sLen)
            throw new IllegalArgumentException(
                    "String length must be a multiple of four.");
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (s.charAt(sLen-1) == '=') {
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if (s.charAt(sLen-2) == '=')
                missingBytesInLastGroup++;
        }
        byte[] result = new byte[3*numGroups - missingBytesInLastGroup];

        // Translate all full groups from base64 to byte array elements
        int inCursor = 0, outCursor = 0;
        for (int i=0; i<numFullGroups; i++) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch3 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }

        // Translate partial group, if present
        if (missingBytesInLastGroup != 0) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (missingBytesInLastGroup == 1) {
                int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        // assert inCursor == s.length()-missingBytesInLastGroup;
        // assert outCursor == result.length;
        return result;
    }

    /**
     * Translates the specified character, which is assumed to be in the
     * "Base 64 Alphabet" into its equivalent 6-bit positive integer.
     *
     * @throw IllegalArgumentException or ArrayOutOfBoundsException if
     *        c is not in the Base64 Alphabet.
     */
    private static int base64toInt(char c, byte[] alphaToInt) {
        int result = alphaToInt[c];
        if (result < 0)
            throw new IllegalArgumentException("Illegal character " + c);
        return result;
    }

    /**
     * This array is a lookup table that translates unicode characters
     * drawn from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045)
     * into their 6-bit positive integer equivalents.  Characters that
     * are not in the Base64 alphabet but fall within the bounds of the
     * array are translated to -1.
     */
    private static final byte base64ToInt[] = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
            55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
            5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
            24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    /**
     * This array is the analogue of base64ToInt, but for the nonstandard
     * variant that avoids the use of uppercase alphabetic characters.
     */
    private static final byte altBase64ToInt[] = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1,
            2, 3, 4, 5, 6, 7, 8, -1, 62, 9, 10, 11, -1 , 52, 53, 54, 55, 56, 57,
            58, 59, 60, 61, 12, 13, 14, -1, 15, 63, 16, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, 17, -1, 18, 19, 21, 20, 26, 27, 28, 29, 30, 31, 32, 33,
            34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
            51, 22, 23, 24, 25
    };

}
