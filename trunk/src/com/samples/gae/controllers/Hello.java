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
package com.samples.gae.controllers;

import com.zipeg.gae.*;

import java.util.*;

/*  uri "/ping/hello" will dispatch to Hello.hello and "/hello/ping" to Ping.ping
    both GET and POST are dispatched to the same endpoint, use req.getMethod() if
    the differentiation is needed. All the parameters in their original names and
    values are put() into Context map. They are also assigned to controller fields
    if the names and types match. "parameter-names-with-dashes" translated to
    "parameter_names_with_dashes".
    Only public void methods without parameters may serve as endpoints

    possible tests:
    http://localhost:8080/hello?date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true
    or
    curl -d "date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true" http://localhost:8080/hello
*/

/** @noinspection UnusedDeclaration */
public final class Hello extends Context {

    int number;
    long big;
    String string;
    Date date;
    boolean b;

    public void hello() {
        String s = "Context {<br />";
        for (Map.Entry<String, Object> e : entrySet()) {
            s += "&nbsp;&nbsp;&nbsp;&nbsp;" + e.getKey() + "=\"" + e.getValue() + "\"<br />";
        }
        s += "}<br />";
        echo("<html><body>");
        echo("hello " + req.getMethod());
        echo("number=" + number + " long=" + big);
        echo(" string=\""+ string + "\"" + " date="+ date + " b="+ b + "<br />");
        echo(s);
        echo("</body></html>");
    }

    private void hidden() {
        echo("<html><body>hidden should not be called ever</body></html>");
    }

}
