package com.zipeg.gae;

import javax.servlet.http.*;
import java.util.*;

import static com.zipeg.gae.util.*;

public final class Cookies extends HashMap<Object, Object> {

    private static ThreadLocal<Cookies> tl = new ThreadLocal<Cookies>();
    private boolean delete;

    public static Cookies getInstance() {
        Cookies c = tl.get();
        if (c == null) {
            c = new Cookies();
            tl.set(c);
        }
        return c;
    }

    public static Cookie encode() {
        Cookies c = getInstance();
        String e = crypto.encryptJson(c);
        Context ctx = Context.get();
        String VERSION = str.defau1t(ctx.server.get("COOKIE_VERSION"), "1");
        String NAME = str.defau1t(ctx.server.get("COOKIE_NAME"), "my_cookie");
        String AGE = str.defau1t(ctx.server.get("COOKIE_AGE_SECONDS"), "" + 3600 * 24 * 5);
        Cookie cookie = new Cookie(NAME, e);
        cookie.setPath("/");
        cookie.setDomain(ctx.req.getServerName());
        cookie.setSecure(ctx.serverURL.contains("https://"));
        if (c.delete || str.isEmpty(e)) {
            cookie.setMaxAge(0); // delete cookie
        } else {
            cookie.setMaxAge(a2i(AGE));
        }
        cookie.setVersion(a2i(VERSION));
        return cookie;
    }

    public static void decode() {
        Context ctx = Context.get();
        String VERSION = str.defau1t(ctx.server.get("COOKIE_VERSION"), "1");
        String NAME = str.defau1t(ctx.server.get("COOKIE_NAME"), "my_cookie");
        Cookie[] cs = ctx.req.getCookies();
        boolean decoded = false;
        if (cs != null) {
            for (Cookie c : cs) {
                if (NAME.equals(c.getName()) && a2i(VERSION) == c.getVersion()) {
                    try {
                        if (decoded) {
                            trace("ERROR: duplicate cookies");
                            delete();
                        } else {
                            getInstance().putAll(crypto.decryptJson(c.getValue()));
                        }
                        decoded = true;
                    } catch (Throwable e) {
                        trace("failed to decode cookie: " + e.getMessage() + " " + c);
                    }
                }
            }
        }
    }

    public static void delete() {
        getInstance().delete = true;
    }

    private Cookies() { }

}
