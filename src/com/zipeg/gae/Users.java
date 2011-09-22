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
import java.util.*;

import static com.zipeg.gae.util.*;

public class Users implements UserService  {
    
    private static final ThreadLocal<Users> tl = new ThreadLocal<Users>();
    private final UserService us = UserServiceFactory.getUserService();

    public static synchronized Users getUserService() {
        if (tl.get() == null) {
            tl.set(new Users());
        }
        return tl.get();
    }

    public String createLoginURL(String destinationURL) {
        return createLoginURL(destinationURL, null);
    }

    public String createLoginURL(String destinationURL, String authDomain) {
        return createLoginURL(destinationURL, authDomain, null, null);
    }

    public String createLoginURL(String destinationURL, String authDomain,
            String federatedIdentity, Set<String> attributesRequest) {
        if (isFacebook(federatedIdentity)) {
            // http://developers.facebook.com/docs/authentication/
            // http://developers.facebook.com/docs/reference/dialogs/oauth/
            // http://developers.facebook.com/docs/reference/dialogs/#display
            // http://developers.facebook.com/docs/reference/api/permissions/
            String scope = "user_about_me,email"; // this is default for facebook
            if (attributesRequest != null) {
                for (String a : attributesRequest) {
                    if (a.startsWith("fb=")) {
                        scope = a.substring(3).trim();
                    }
                }
            }
            return Context.localURL("/facebook_auth?scope=" + scope + "&destination=" +
                    encodeURL(destinationURL));
/*
            return "https://www.facebook.com/dialog/oauth?client_id=" + Context.get().fbAppId +
            "&redirect_uri=" + Context.get().serverURL + "facebook_auth" +
            "&state=" + Context.get().res.encodeURL(destinationURL) +
            "&scope=" + scope +
            "&response_type=signed_request"; // "code" "token" or "code token"
            "&display=touch"; // page, popup, touch, or wap. (iframe documented but unsupported)
*/
        } else {
            return us.createLoginURL(destinationURL, authDomain, federatedIdentity, attributesRequest);
        }
    }

    public String createLogoutURL(String destinationURL) {
        // https://www.facebook.com/logout.php?next=YOUR_URL&access_token=ACCESS_TOKEN
        return createLogoutURL(destinationURL, null);
    }

    public String createLogoutURL(String destinationURL, String authDomain) {
        if (Context.get().userInfo != null) {
            return Context.localURL("/facebook_logout?destination=" + encodeURL(destinationURL));
        } else {
            return us.createLogoutURL(destinationURL, authDomain);
        }
    }

    public boolean isUserLoggedIn() {
        if (Context.get().fbUser == null) {
            String uid = Context.get().getUserInfo("uid");
            String email = Context.get().getUserInfo("email");
            if (!str.isEmpty(uid) && !str.isEmpty(email)) {
                Context.get().fbUser = new User(email, "", uid, "https://wwww.facebook.com/");
            }
        }
        return Context.get().fbUser != null || us.isUserLoggedIn();
    }

    public boolean isUserAdmin() {
        return Context.get().userInfo != null ?
                "true".equals(Context.get().getUserInfo("admin")) : us.isUserAdmin();
    }

    public User getCurrentUser() {
        return Context.get().fbUser != null ? Context.get().fbUser : us.getCurrentUser();
    }

    private boolean isFacebook(String fi) {
        return !str.isEmpty(fi) && fi.startsWith("https://www.facebook.com/");
    }

}
