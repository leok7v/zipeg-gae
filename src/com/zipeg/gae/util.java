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

    public static String encodeURL(String s) {
        try {
            return URLEncoder.encode(str.null2empty(s), "UTF-8");
        } catch (UnsupportedEncodingException e) {
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
        try { return Long.parseLong(str.trim(a)); } catch (NumberFormatException t) { return null; }
    }

    public static Integer a2i(String a) {
        try { return Integer.parseInt(str.trim(a)); } catch (NumberFormatException t) { return null; }
    }

    public static Boolean a2b(String a) {
        return Boolean.parseBoolean(str.trim(a)); }

    public static Double a2d(String a) {
        try { return Double.parseDouble(str.trim(a)); } catch (NumberFormatException t) { return null; }
    }

    public static Float a2f(String a) {
        try { return Float.parseFloat(str.trim(a)); } catch (NumberFormatException t) { return null; }
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
            return str.isEmpty(n) ? null : Class.forName(n);
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
