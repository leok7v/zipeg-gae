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

import com.google.appengine.api.users.*;
import com.zipeg.gae.*;

import java.io.*;
import java.util.*;

/** @noinspection UnusedDeclaration */
public class Auth extends Context {

    private final UserService us = UserServiceFactory.getUserService();

    // http://groups.google.com/group/google-appengine/browse_thread/thread/2e0c459c14cde662
    private static final String[][] openIdProviders = new String[][] {
        new String[] {"Google", "www.google.com/accounts/o8/id"},
        new String[] {"Yahoo", "yahoo.com"},
        new String[] {"MySpace", "myspace.com"},
        new String[] {"AOL", "aol.com"},
        new String[] {"MyOpenId.com", "myopenid.com"}
    };

    String openid_identifier;
    String openid_username;
    String returnURL;

    public void signin() throws IOException {
        Set<String> attributes = new HashSet<String>();
        if (us.isUserLoggedIn()) {
            User u = us.getCurrentUser(); // or req.getUserPrincipal()
            echo("Hello <i>" + u.getNickname() + "</i>!<br />");
            echo("user.getEmail()=" + u.getEmail() + "<br />");
            echo("user.getUserId()=" + u.getUserId() + "<br />");
            echo("user.getFederatedIdentity()=" + u.getFederatedIdentity() + "<br />");
            echo("user.AuthDomain()=" + u.getAuthDomain() + "<br />");
            echo("userService.isUserLoggedIn()=" + us.isUserLoggedIn() + "<br />");
            echo("userService.isUserAdmin()=" + us.isUserAdmin() + "<br />");
            echo("[<a href=\""
                    + us.createLogoutURL(req.getRequestURI())
                    + "\">sign out</a>]");
        } else {
            if (!util.isEmpty(openid_identifier)) {
                if (util.isEmpty(returnURL)) {
                    returnURL = req.getRequestURI();
                }
                String loginUrl = us.createLoginURL(returnURL, null, openid_identifier, attributes);
                sendRedirect(loginUrl);
            } else {
                sendRedirect("/oid");
/*
                echo("Sign in at: ");
                for (String[] pair : openIdProviders) {
                    String providerUrl = pair[1];
                    String loginUrl = us.createLoginURL(req.getRequestURI(), null, pair[1], attributes);
                    echo("[<a href=\"" + loginUrl + "\">" + pair[0] + "</a>] ");
                }
*/
            }
        }
    }

    public void signout() throws IOException {
        if (us.isUserLoggedIn()) {
            sendRedirect(us.createLogoutURL(req.getRequestURI()));
        }
    }

}