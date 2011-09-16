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

    public static synchronized void set(Context ctx) {
        assert (get() == null) != (ctx == null) : "get()=" + get() + " ctx=" + ctx;
        tl.set(ctx);
    }

    public static synchronized Context get() {
        return tl.get();
    }

    public String head() {
        return ""; // override and return content of <head></head>
    }

    public String body() {
        return ""; // override and return content of <body></body>
    }

    public void echo(String s) { // cannot be used with forwarding to jsp/jspf views
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
