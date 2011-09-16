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

import com.google.appengine.api.memcache.*;

import javax.jdo.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import static com.zipeg.gae.util.*;

public class servlet extends HttpServlet {

    static {
        // http://groups.google.com/group/google-appengine/msg/cb538fa9b024f362
        servlet.class.getClassLoader().setDefaultAssertionStatus(true);
        // http://code.google.com/appengine/docs/java/howto/maintenance.html
        MemcacheServiceFactory.getMemcacheService().setErrorHandler(new StrictErrorHandler());
    }

    private static final PersistenceManagerFactory pmf = // long init
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    protected void service(HttpServletRequest req, HttpServletResponse res) throws
            ServletException, IOException {
        timestamp("service");
        // servlet is dispatching dynamic content that should not be cached
        res.setCharacterEncoding("utf-8");
        res.setDateHeader("Expires", System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.addHeader("Cache-Control", "post-check=0, pre-check=0");
        res.setHeader("Pragma", "no-cache");
        if (!dispatcher.dispatch(getServletContext(), req, res, pmf.getPersistenceManagerProxy())) {
            super.service(req, res);
        }
        timestamp("service");
    }

}
