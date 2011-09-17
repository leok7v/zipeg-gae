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

import javax.jdo.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import static com.zipeg.gae.util.*;

public class Context extends HashMap<String, Object> {

    private static final PersistenceManagerFactory pmf = // long init
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private static ThreadLocal<Context> tl = new ThreadLocal<Context>();

    public String[] path; // uri="/foo/bar" results in path={"foo", "bar"}
    public String view;  // can be null
    public String server;  // http[s]://localhost:8080/ (with trailing slash
    public HttpServletRequest  req;
    public HttpServletResponse res;
    public String revision = ""; // svn or other vcs revision
    public final PersistenceManager  pm = pmf.getPersistenceManagerProxy();
    private PrintWriter echoWriter;
    private boolean redirected;
    private StringBuilder head = new StringBuilder();
    private StringBuilder body = new StringBuilder();

    public static synchronized void set(Context ctx) {
        assert (get() == null) != (ctx == null) : "get()=" + get() + " ctx=" + ctx;
        tl.set(ctx);
    }

    public static synchronized Context get() {
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
        if (!util.isEmpty(s)) {
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

}
