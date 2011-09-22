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
import com.samples.gae.model.*;
import com.zipeg.gae.*;

import java.io.*;
import java.util.*;

import com.google.appengine.api.datastore.*;

import static com.zipeg.gae.util.*;

// http://code.google.com/apis/accounts/docs/OpenID.html

/** @noinspection UnusedDeclaration */
public class Auth extends Context {

    private final Users us = Users.getUserService();

    String openid_identifier;
    String openid_username;
    String returnURL;

    public void signin() throws IOException {
        // http://openid.net/specs/openid-authentication-2_0.html#anchor27
        Set<String> attributes = new HashSet<String>();
        attributes.add("openid.ax.required=email,firstname,lastname");
        attributes.add("fb=user_about_me,email");
        if (us.isUserLoggedIn()) {
            User u = us.getCurrentUser(); // or req.getUserPrincipal()
            echo("Hello <i>" + u.getNickname() + "</i>!<br />");
            echo("req.getUserPrincipal()=" + req.getUserPrincipal() + "<br />");
            echo("user.getEmail()=" + u.getEmail() + "<br />");
            echo("user.getUserId()=" + u.getUserId() + "<br />");
            echo("user.getFederatedIdentity()=" + u.getFederatedIdentity() + "<br />");
            echo("user.AuthDomain()=" + u.getAuthDomain() + "<br />");
            echo("userService.isUserLoggedIn()=" + us.isUserLoggedIn() + "<br />");
            echo("userService.isUserAdmin()=" + us.isUserAdmin() + "<br />");
            echo("[<a href=\""
                    + us.createLogoutURL(req.getRequestURI())
                    + "\">sign out</a>]");
            timestamp("query");
            // noinspection unchecked
            Collection<Account> as = (Collection<Account>)
                    pm.newQuery(Account.class, "id == '" + u.getUserId() + "'").execute();
            timestamp("query");
            Account a;
            if (as != null && as.size() > 0) {
                assert as.size() == 1 : "as.size()=" + as.size() + " for u.getUserId()=" + u.getUserId();
                a = as.iterator().next();
            } else {
                a = new Account();
                a.id = u.getUserId();
            }
            assert obj.equal(a.id, u.getUserId()) : "a.id=" + a.id + " u.getUserId()=" + u.getUserId();
            a.nickname = u.getNickname();
            a.email = u.getEmail();
            a.authDomain = u.getAuthDomain();
            a.federatedIdentity = u.getFederatedIdentity();
            timestamp("makePersistent");
            pm.makePersistent(a);
            timestamp("makePersistent");
            Key k = KeyFactory.createKey(Account.class.getSimpleName(), u.getUserId());
            timestamp("getObjectById");
            Account v = pm.getObjectById(Account.class, k);
            timestamp("getObjectById");
            assert a.equals(v);
        } else {
            if (!str.isEmpty(openid_identifier)) {
                if (str.isEmpty(returnURL)) {
                    returnURL = req.getRequestURI();
                }
                String loginUrl = us.createLoginURL(returnURL, null, openid_identifier, attributes);
                sendRedirect(loginUrl);
            } else {
                sendRedirect("/oid");
            }
        }
    }

    public void signout() throws IOException {
        if (us.isUserLoggedIn()) {
            sendRedirect(us.createLogoutURL(req.getRequestURI()));
        }
    }

}
