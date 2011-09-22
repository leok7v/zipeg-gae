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

import com.google.appengine.api.users.*;

import javax.jdo.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import static com.zipeg.gae.io.*;
import static com.zipeg.gae.util.*;

public class Context extends HashMap<String, Object> {

    private static final PersistenceManagerFactory pmf = // make take long time to init
        JDOHelper.getPersistenceManagerFactory("transactions-optional");
    public static final SecureRandom random = new SecureRandom();

    private static ThreadLocal<Context> tl = new ThreadLocal<Context>();

    public String[] path; // uri="/foo/bar" results in path={"foo", "bar"}
    public String view;  // can be null
    public String serverURL;  // http[s]://localhost:8080/ (with trailing slash)
    public HttpServletRequest  req;
    public HttpServletResponse res;
    public String revision = ""; // svn or other vcs revision
    public String fbAppId;
    public String fbAppSecret;
    public User   fbUser;
    public Map<String, String> userInfo;
    public Map<String, String> server; // unpacked server.properties file
    public final PersistenceManager  pm = pmf.getPersistenceManagerProxy();
    private PrintWriter echoWriter;
    private boolean redirected;
    private StringBuilder head = new StringBuilder();
    private StringBuilder body = new StringBuilder();

    public static void set(Context ctx) {
        assert (get() == null) != (ctx == null) : "get()=" + get() + " ctx=" + ctx;
        tl.set(ctx);
    }

    public static Context get() {
        return tl.get();
    }

    public boolean isRedirected() {
        return redirected;
    }

    public boolean hasOutput() {
        return echoWriter != null;
    }

    public String head() {
        return head.toString(); // override and return content of <head></head> and call super
    }

    public String body() {
        return body.toString(); // override and return content of <body></body> and call super
    }

    public void echo(String s) { // cannot be used with forwarding to jsp/jspf views
        if (!str.isEmpty(s)) {
            try {
                if (echoWriter == null) {
                    echoWriter = res.getWriter();
                }
                echoWriter.println(s);
                // do not close output stream so the echo can be used repeatedly.
            } catch (IOException e) {
                rethrow(e);
            }
        }
    }

    public void appendToHead(String s) {
        if (hasOutput()) {
            throw new Error("already used echo() cannot append to head");
        }
        head.append(s);
    }

    public void appendToBody(String s) {
        if (hasOutput()) {
            throw new Error("already used echo() cannot append to body");
        }
        body.append(s);
    }

    // to keep dispatcher functioning correctly, please use this version of sendRedirect()
    // instead of directly calling res.sendRedirect(res.encodeRedirectURL(url))
    public static void sendRedirect(String unencodedURL) {
        try {
            get().res.sendRedirect(get().res.encodeRedirectURL(unencodedURL));
            get().redirected = true;
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public static String localURL(String endpoint_and_query) {
        String s = get().serverURL;
        assert s.endsWith("/") : "unexpected serverURL=" + s;
        String eq = str.null2empty(endpoint_and_query);
        return s + (eq.startsWith("/") ? eq.substring(1) : eq);
    }

    public static String post(String endpoint, String data) {
        java.net.HttpURLConnection c = null;
        try {
            c = (java.net.HttpURLConnection)new URL(endpoint).openConnection();
            c.setRequestMethod("POST");
            c.setUseCaches(false);
            c.setDoInput(true);
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            c.setRequestProperty("Content-Length", "" + data.length());
            OutputStreamWriter wr = new OutputStreamWriter(c.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
            int code = c.getResponseCode();
            if (code == 200) {
                byte[] raw = readFullyAndClose(c.getInputStream());
                return str.trim(new String(raw));
            } else {
                trace("postData(" + endpoint + ", " + data +") returned " + code);
                Thread.dumpStack();
                return "";
            }
        } catch (Throwable e) {
            // MalformedURLException, ProtocolException, IOException
            trace("post(" + endpoint + ", " + data +") failed " + e.getMessage());
            e.printStackTrace();
            return "";
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

    public static String post(String endpoint, Map<String, String> fd) {
        StringBuilder sb = new StringBuilder(fd.size() * 100);
        for (Map.Entry<String, String> e : fd.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(e.getKey(), e.getValue()));
        }
        return post(endpoint, sb.toString());
    }

    private static String encode(String n, String v) {
        return encodeURL(n) + "=" + encodeURL(v);
    }

    public String getUserInfo(String field) {
        if (userInfo == null) {
            // noinspection unchecked
            userInfo = (Map<String, String>)Cookies.getInstance().get("user_info");
        }
        if (userInfo != null) {
            return userInfo.get(field);
        } else {
            return null;
        }
    }

}
