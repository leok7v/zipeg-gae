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

// to test: http://localhost:8080/fragment

/** @noinspection UnusedDeclaration */
public class Fragment extends Context {

    public String text;

    public void fragment() {
        text = "fragment() was called";
        /* echo("using echo will break fragment.jspf forwarding"); */
    }

    public String head() {
        return "<br>this text is the returned value of head() call that will appended " +
                "to the bottom of the &lt;head&gt;&lt;/head&gt;<br>";
    }

    public String body() {
        return "<br>this text is the returned value of body() call that will appended " +
                "to the bottom of the &lt;body&gt;&lt;/body&gt;<br>";
    }

}
