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

import com.google.appengine.api.quota.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.text.*;
import java.util.*;

/** @noinspection UnusedDeclaration */
public class util {

    // Internet Engineering Task Force Date format:
    private static final DateFormat IETF = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    // Default Date().toString() format:
    private static final DateFormat DATE = new SimpleDateFormat("EEE, MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
    // http://en.wikipedia.org/wiki/ISO_8601 aka ZULU time Date format
    private static final DateFormat ZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final DateFormat[] dateParsers = new DateFormat[]{IETF, DATE, ZULU};

    private static final ThreadLocal<Map<String, Map<String, Long>>> timestamps =
            new ThreadLocal<Map<String, Map<String, Long>>>();
    private static final QuotaService qs = QuotaServiceFactory.getQuotaService();

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String null2empty(String s) {
        return s == null ? s : s.trim();
    }

    public static String defau1t(String s, String defau1t) {
        return isEmpty(s) ? defau1t : s;
    }

    public static boolean equal(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2) || o2.equals(o1);
    }

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }

    public static String encodeURL(String s) {
        try {
            return URLEncoder.encode(null2empty(s), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
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

    private static final char[] hex = "0123456789ABCDEF".toCharArray();

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

    private static int nibble(char ch) {
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

    public static String b2a(Object v) {
        if (v instanceof byte[]) {
            try {
                return new String((byte[])v, "UTF-8");
            } catch (UnsupportedEncodingException x) {
                rethrow(x);
            }
        }
        return v.toString();
    }

    public static Long a2l(String a) {
        try { return Long.parseLong(trim(a)); } catch (NumberFormatException t) { return null; }
    }

    public static Integer a2i(String a) {
        try { return Integer.parseInt(trim(a)); } catch (NumberFormatException t) { return null; }
    }

    public static Boolean a2b(String a) {
        return Boolean.parseBoolean(trim(a)); }

    public static Double a2d(String a) {
        try { return Double.parseDouble(trim(a)); } catch (NumberFormatException t) { return null; }
    }

    public static Float a2f(String a) {
        try { return Float.parseFloat(trim(a)); } catch (NumberFormatException t) { return null; }
    }

    public static String d2s(Date d) {
        return IETF.format(d);
    }

    public static Date s2d(String s) {
        for (DateFormat p : dateParsers) {
            try { return p.parse(s); } catch (ParseException e) { /* ignore */ }
        }
        trace("WARNING: failed to parse Date: " + s);
        return null;
    }

    public static void trace(String s) {
        println(new Date() + " " +
                getCallerClass().getSimpleName() + "." + getCallerMethod() +
                "(" + getCallerLineNumber() + ")  " + s);
    }

    public static void println(String s) {
        System.out.println(s);
    }

    public static Class<?> forName(String n) {
        try {
            return isEmpty(n) ? null : Class.forName(n);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> getCallerClass() {
        return forName(Thread.currentThread().getStackTrace()[3].getClassName());
    }

    public static String getCallerMethod() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    public static int getCallerLineNumber() {
        return Thread.currentThread().getStackTrace()[3].getLineNumber();
    }

    public static void rethrow(Throwable t) throws Error {
        throw t instanceof Error ? (Error)t : new Error(t);
    }

    public static Throwable unwind(Throwable t) {
        while (t != null && t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    public static boolean exists(File f) {
        try {
            return f.exists();
        } catch (AccessControlException x) {
            return false;
        }
    }

    public static boolean isDirectory(File f) {
        try {
            return f.isDirectory();
        } catch (AccessControlException x) {
            return false;
        }
    }

    public static void timestamp(String key) { // returns delta in nanoseconds
        Map<String, Map<String, Long>> m;
        m = timestamps.get();
        if (m == null) {
            m = new HashMap<String, Map<String, Long>>();
            timestamps.set(m);
        }
        Map<String, Long> t = m.remove(key);
        if (t == null) {
            t = new HashMap<String, Long>();
            t.put("cpuStart", qs.getCpuTimeInMegaCycles());
            t.put("apiStart", qs.getApiTimeInMegaCycles());
            t.put("sysStart", System.nanoTime());
            m.put(key, t);
        } else {
            long cpuEnd = qs.getCpuTimeInMegaCycles();
            long apiEnd = qs.getApiTimeInMegaCycles();
            long sysEnd = System.nanoTime();
            long delta = sysEnd - t.get("sysStart");
            double cpuSeconds = qs.convertMegacyclesToCpuSeconds(cpuEnd - t.get("cpuStart"));
            double apiSeconds = qs.convertMegacyclesToCpuSeconds(apiEnd - t.get("apiStart"));
            println(new Date() + " " +
                    getCallerClass().getSimpleName() + "." + getCallerMethod() +
                    "(" + getCallerLineNumber() + ")  " +
                    key + " cpu: " + d4(cpuSeconds) + "s api: " + d4(apiSeconds) +
                    "s wall: " + humanReadableNanoseconds(delta));
        }
    }

    public static String humanReadableNanoseconds(long delta) {
        if (delta < 10L * 1000) {
            return delta + " nanoseconds";
        } else if (delta < 10L * 1000 * 1000) {
            return delta / 1000 + " microseconds";
        } else if (delta < 10L * 1000 * 1000 * 1000) {
            return delta / (1000 * 1000) + " milliseconds";
        } else {
            return delta / (1000 * 1000 * 1000) + " seconds";
        }
    }

    private static double d4(double v) {
        return ((long)(v * 10000)) / 10000.;
    }

    private util() {}

}
