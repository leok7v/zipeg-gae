/* Zipeg for Google App Engine components License

 Copyright (c) 2006-2011, Leo Kuznetsov
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.
 Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 Neither the name of the Zipeg nor the names of its contributors may be used
 to endorse or promote products derived from this software without specific
 prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 http://www.opensource.org/licenses/BSD-3-Clause
*/
package com.zipeg.gae;

import java.io.*;

/** @noinspection UnusedDeclaration */
public class str {

    public static final char[] hex = "0123456789ABCDEF".toCharArray();

    private str() {}

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String null2empty(String s) {
        return s == null ? "" : s.trim();
    }

    public static String defau1t(String s, String defau1t) {
        return isEmpty(s) ? defau1t : s;
    }

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }

    public static String fromUTF8(byte[] ascii) {
        try {
            return new String(ascii, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static byte[] toUTF8(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static String toHex(byte[] raw) {
        if (raw == null || raw.length <= 0) {
            return "";
        }
        StringBuilder out = new StringBuilder(raw.length * 2);
        for (byte b : raw) {
            out.append(hex[(b >> 4) & 0x0F]);
            out.append(hex[(b     ) & 0x0F]);
        }
        return out.toString();
    }

    public static int nibble(char ch) {
        if (ch >= '0' && ch <= '9') return (ch - '0');
        if (ch >= 'a' && ch <= 'f') return (ch - 'a') + 0x0A;
        if (ch >= 'A' && ch <= 'F') return (ch - 'A') + 0x0A;
        return -1;
    }

    public static byte[] fromHex(String s) {
        if (s == null) {
            throw new NullPointerException("s is null");
        }
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException("s=" + s);
        }
        s = s.trim().toLowerCase();
        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < data.length; ++i) {
            int h = nibble(s.charAt(i * 2));
            int l = nibble(s.charAt(i * 2 + 1));
            assert h >= 0 && l >=0;
            data[i] = (byte)((h & 0x0F) << 4 | (l & 0x0F));
        }
        return data;
    }

    public static String hexEncode(String s) {
        try {
            return isEmpty(s) ? s : toHex(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException x) {
            throw new Error(s);
        }
    }

    public static String hexDecode(String s) {
        try {
            return isEmpty(s) ? s : new String(fromHex(s), "UTF-8");
        } catch (UnsupportedEncodingException x) {
            throw new Error(s);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

}
