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
import com.google.appengine.repackaged.com.google.common.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.net.*;
import java.util.*;

import static com.zipeg.gae.io.*;
import static com.zipeg.gae.util.*;

/** @noinspection UnusedDeclaration */
public class FacebookAuth extends Context  {

    public void facebook_logout() {
        String token = getUserInfo("token");
        Cookies.getInstance().remove("user_info");
        if (!str.isEmpty(token)) {
            String u = "https://www.facebook.com/logout.php?next=" + encodeURL(destination) +
                    "&access_token=" + token;
            echo("<!DOCTYPE html>" +
                    "<html> \n" +
                    "   <head> \n" +
                    "     <title>Facebook</title> \n" +
                    "   </head> \n" +
                    "   <body> \n" +
                    "   <script> \n" +
                    "     var url = '" + u + "';\n" +
                    "     if (window.frameElement) {\n" +
                    "        window.frameElement.src = url;\n" +
                    "     } else {\n" +
                    "        top.location.href = url;\n" +
                    "     }\n" +
                    "   </script> \n" +
                    "   </body> \n" +
                    "  </html>");
        }
        userInfo = null;
        fbUser = null;
        Cookies.delete();
    }

    public String scope;
    public String destination;

    public void facebook_auth() {
        echo("<!DOCTYPE html>" +
                "<html> \n" +
                "   <head> \n" +
                "     <title>Facebook</title> \n" +
                "   </head> \n" +
                "   <body> \n" +
                "   <script> \n" +
                "     var appID = '" + Context.get().fbAppId + "';\n" +
                "     if (window.location.hash.length == 0) {\n" +
                "        var path = 'https://www.facebook.com/dialog/oauth?';\n" +
                "        var queryParams = ['client_id=' + appID,\n" +
                "          'redirect_uri=' + window.location,\n" +
                "          'state=" + destination + "',\n" +
                "          'scope=" + scope + "',\n" +
                "          'display=touch',\n" +
                "          'response_type=signed_request'];\n" +
                "        var query = queryParams.join('&');\n" +
                "        var url = path + query;\n" +
                "        if (window.frameElement) {\n" +
                "           window.frameElement.src = url;\n" +
                "        } else {\n" +
                "           top.location.href = url;\n" +
                "        }\n" +
                "     } else {\n" +
                "        var res = window.location.hash.substring(1);\n" +
                "        /* console.log(res); */ \n" +
                "        var url = '" + localURL("facebook_auth_response") + "?' + res;\n" +
                "        if (window.frameElement) {\n" +
                "           window.frameElement.src = url;\n" +
                "        } else {\n" +
                "           top.location.href = url;\n" +
                "        }\n" +
                "     }\n" +
                "   </script> \n" +
                "   </body> \n" +
                "  </html>");
    }

    public String state;
    public String signed_request;
    public String error_reason;
    public String error;
    public String code;
    public String access_token;
    public String error_description;

    /** @noinspection ConstantConditions */
    public void facebook_auth_response() {
/*
        http://localhost:8080/facebook_auth#state=%2Fsignin&access_token=AAABraORWNzkBAIkawwpw90ydGFZByvObLuEdqZBxYFPeNcMBmbV58rDPzuKRpXitXyw1N3KhDMQ8XupepzcZBlIPKLZBbmZCTppIHneNzvjcWvdDD75sE&expires_in=4289
*/
        trace(">facebook_auth_response" +
                "\n state=" + state +
                "\n error=" + error +
                "\n error_reason=" + error_reason +
                "\n error_description=" + error_description +
                "\n signed_request=" + signed_request);
        if (str.isEmpty(error)) {
            if (!str.isEmpty(signed_request)) {
                try {
                    Map<Object, Object> m = parse_signed_request(signed_request, fbAppSecret);
                    Date issued_at = new Date((Long)m.get("issued_at"));
                    String uid = (String)m.get("user_id");
                    String code = (String)m.get("code");
                    String token = (String)m.get("token");
                    String email = (String)m.get("email");
                    String first = (String)m.get("first_name");
                    String last = (String)m.get("last_name");
                    String username = (String)m.get("username");
                    if (str.isEmpty(token) && !str.isEmpty(code) && !str.isEmpty(uid)) {
                        try {
                            URL url = new URL("https://graph.facebook.com/oauth/access_token?" +
                                    "client_id=" + Context.get().fbAppId +
                                    "&client_secret=" + Context.get().fbAppSecret +
                                    "&code=" + code +
                                    "&state=" + state +
                                    "&type=client_cred" +
                                    "&redirect_uri=" + localURL("/facebook_auth_response"));
                            String response = new String(readFullyAndClose(url.openStream()));
                            String[] parts = response.split("[=&]");
                            if (parts.length >= 2 && "access_token".equalsIgnoreCase(parts[0])) {
                                token = parts[1];
                            }
                        } catch (MalformedURLException e) {
                            throw new Error(e);
                        } catch (IOException e) {
                            throw new Error(e);
                        }
                    }
                    if (!str.isEmpty(token) && (str.isEmpty(uid) || str.isEmpty(email) ||
                        str.isEmpty(first) || str.isEmpty(last))) {
                        try {
                            // http://developers.facebook.com/docs/reference/api/user/
                            String u = "https://graph.facebook.com/" +
                                    (str.isEmpty(uid) ? "me" : uid) +
                                    "&state=" + state +
                                    "&access_token=" + encodeURL(token);
                            URL url = new URL(u);
                            String s = new String(readFullyAndClose(url.openStream()));
                            Map<Object, Object> map = json.decode(s);
                            uid = (String)map.get("id");
                            email = (String)map.get("email");
                            first = (String)map.get("first_name");
                            last = (String)map.get("last_name");
                            username = (String)map.get("username");
                        } catch (MalformedURLException e) {
                            throw new Error(e);
                        } catch (IOException e) {
                            throw new Error(e);
                        }
                    }
                    trace("uid=" + uid);
                    trace("token=" + token);
                    trace("email=" + email);
                    trace("username=" + username);
                    trace("first=" + first);
                    trace("last=" + last);
                    trace("issued_at=" + issued_at);
                    if (str.isEmpty(email) && !str.isEmpty(username)) {
                        email = username + "@facebook.com";
                    }
                    userInfo = new HashMap<String, String>();
                    userInfo.put("uid", uid);
                    userInfo.put("email", email);
                    userInfo.put("first_name", first);
                    userInfo.put("last_name", last);
                    userInfo.put("name", first + " " + last);
                    userInfo.put("auth", "https://www.facebook.com/");
                    userInfo.put("username", token);
                    userInfo.put("token", token);
                    userInfo.put("admin", "false");
                    Cookies.getInstance().put("user_info", userInfo);
                    fbUser = new User(email, "", uid, "https://wwww.facebook.com/");
                } catch (Exception e) {
                    error = e.getMessage();
                }
            }
            if (!str.isEmpty(access_token)) {
                
            }
        }
        String u = localURL(state);
        sendRedirect(u);
    }

    public static byte[] base64_url_decode(String input) throws Base64DecoderException {
        return Base64.decodeWebSafe(input);
    }

    public static Map<Object, Object> parse_signed_request(String s, String secret) throws Exception {
        return parse_signed_request(s, secret, 3600);
    }

    public static Map<Object, Object> parse_signed_request(String input, String secret, int max_age)
            throws Exception {
        String[] split = input.split("[.]", 2);
        String encoded_sig = split[0];
        String encoded_envelope = split[1];
        Map<Object, Object> envelope = json.decode(new String(base64_url_decode(encoded_envelope)));
        String algorithm = (String)envelope.get("algorithm");
        if (!algorithm.equals("AES-256-CBC HMAC-SHA256") && !algorithm.equals("HMAC-SHA256")) {
            throw new Exception("Invalid request. (Unsupported algorithm.)");
        }
        if (((Long)envelope.get("issued_at")) < System.currentTimeMillis() / 1000 - max_age) {
            throw new Exception("Invalid request. (Too old.)");
        }
        byte[] key = secret.getBytes();
        SecretKey hmacKey = new SecretKeySpec(key, "HMACSHA256");
        Mac mac = crypto.getMac("HMACSHA256");
        mac.init(hmacKey);
        byte[] digest = mac.doFinal(encoded_envelope.getBytes());
        if (!Arrays.equals(base64_url_decode(encoded_sig), digest)) {
            throw new Exception("Invalid request. (Invalid signature.)");
        }
        // for requests that are signed, but not encrypted, we"re done
        if (algorithm.equals("HMAC-SHA256")) {
            return envelope;
        }
        // otherwise, decrypt the payload
        byte[] iv = base64_url_decode((String)envelope.get("iv"));
        IvParameterSpec ips = new IvParameterSpec(iv);
        SecretKey aesKey = new SecretKeySpec(key, "AES");
        Cipher cipher = crypto.getCipher("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ips);
        byte[] raw_cipher_text = base64_url_decode((String)envelope.get("payload"));
        byte[] plain_text = cipher.doFinal(raw_cipher_text);
        return json.decode(new String(plain_text).trim());
    }

}
