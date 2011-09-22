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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.util.*;

import static java.lang.reflect.Modifier.*;
import static com.zipeg.gae.util.*;
import static com.zipeg.gae.io.*;
import static com.zipeg.gae.str.*;

@SuppressWarnings({"unchecked"})
public class dispatcher {

    private static volatile boolean initialized;
    private static final Map<Class, Map<String, Field>> c2f =
            new HashMap<Class, Map<String, Field>>(1000);
    private static final Map<Class, Set<Class<?>>> c2i =
            new HashMap<Class, Set<Class<?>>>(1000);
    private static final Map<Class, Set<Class<?>>> c2s =
            new HashMap<Class, Set<Class<?>>>(1000);
    private static final Map<String, Method> u2m =
            new HashMap<String, Method>(1000);
    private static final Set<String> contextMethods = new HashSet<String>(10);
    private static Map<String, String> server;
    private static String revision = "wip";

    private dispatcher() { }

    private static void init(ServletContext sc) throws IOException {
        Set<Class<Context>> controllers = new HashSet<Class<Context>>(100);
        getAllClasses(Context.class, controllers);
        collectMethods(controllers, u2m);
        for (Class<Context> c : controllers) {
            trace("controller: " + c.getName());
            collectFields(c);
        }
        Map<String, Method> cm = new HashMap<String, Method>();
        collectMethods(Context.class, cm);
        contextMethods.addAll(cm.keySet());
        readRevision(sc);
        readServerProperties(sc);
    }

    private static void readRevision(ServletContext sc) {
        InputStream is = sc.getResourceAsStream("/WEB-INF/revision.txt");
        try {
            if (is != null) {
                revision = new String(readFully(is)).trim();
            }
        } finally {
            close(is);
        }
    }

    private static void readServerProperties(ServletContext sc) throws IOException {
        InputStream is = sc.getResourceAsStream("/WEB-INF/server.properties");
        try {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                Map<String, String> m = new HashMap<String, String>(p.size() * 2);
                for (Map.Entry<Object, Object> e : p.entrySet()) {
                    if (e.getKey() instanceof String && e.getValue() instanceof String) {
                        m.put((String)e.getKey(), (String)e.getValue());
                    }
                }
                server = Collections.unmodifiableMap(m);
            }
        } finally {
            close(is);
        }
    }

    public static synchronized boolean dispatch(ServletContext sc, HttpServletRequest req,
            HttpServletResponse res) throws IOException, ServletException {
        synchronized (servlet.class) {
            if (!initialized) {
                timestamp("dispatcher.initializeControllers");
                init(sc);
                timestamp("dispatcher.initializeControllers");
                initialized = true;
            }
        }
        String uri = req.getRequestURI();
        if (isEmpty(uri) || "/".equals(uri)) {
            uri = "/index";
        }
        String[] path = uri.substring(1).split("/");
        Context ctx = null;
        Method m = null;
        String endpoint = null;
        RequestDispatcher rd = null;
        for (int i = path.length - 1; i >= 0; i--) {
            endpoint = path[i].toLowerCase();
            // do not dispatch to Context.get() Context.head() Context.body()
            // and any other public void methods() of Context it self
            if (!contextMethods.contains(endpoint)) {
                m = u2m.get(endpoint);
                Class<?> c = m == null ? null : m.getDeclaringClass();
                if (c != null) {
                    // noinspection unchecked
                    Class<Context> cc = (Class<Context>)c;
                    ctx = newInstance(cc);
                    fillContext(ctx, req, res, path, endpoint);
                    processParameters(ctx, req);
                    rd = getRequestDispatcher(sc, ctx.view);
                    break;
                }
            }
        }
        if (ctx == null) {
            for (int i = path.length - 1; i >= 0; i--) {
                endpoint = path[i].toLowerCase();
                rd = getRequestDispatcher(sc, endpoint);
                if (rd != null) {
                    break;
                }
            }
            if (rd != null) {
                ctx = new Context();
                fillContext(ctx, req, res, path, endpoint);
            }
        }
        if (ctx != null) {
            // we cannot do Context.set() in ctor because field collecting instantiates controllers
            Context.set(ctx);
            Cookies.decode();
            try {
                if (m != null) {
                    invoke(m, ctx);
                }
                if (rd != null) {
                    if (ctx.isRedirected() || ctx.hasOutput()) {
                        trace("WARNING: dispatched did not forward to " + endpoint + ".jsp[f]" +
                              " because ctx.isRedirected()=" + ctx.isRedirected() +
                              " or ctx.hasOutput()=" + ctx.hasOutput());
                    } else {
                        rd.forward(req, res);
                    }
                }
                res.addCookie(Cookies.encode());
                return true;
            } finally {
                Context.set(null);
            }
        }
        return false;
    }

    private static void fillContext(Context ctx, HttpServletRequest req, HttpServletResponse res,
            String[] path, String endpoint) {
        ctx.req = req;
        ctx.res = res;
        ctx.path = path;
        ctx.view = endpoint;
        ctx.serverURL =
                (req.isSecure() ? "https://" : "http://") +
                 req.getServerName() +
                (req.getServerPort() == 80 ? "/" : ":" + req.getServerPort() + "/");
        ctx.revision = revision;
        ctx.server = server;
        ctx.fbAppId = server.get(req.getServerName() + ".fbAppId");
        ctx.fbAppSecret = server.get(req.getServerName() + ".fbAppSecret");
    }

    private static RequestDispatcher getRequestDispatcher(ServletContext sc, String view) {
        String fileName = "/WEB-INF/views/" + view + ".jsp";
        File file = new File(sc.getRealPath(fileName));
        if (file.exists() && file.isFile() && file.canRead()) {
            RequestDispatcher rd = sc.getRequestDispatcher(fileName);
            if (rd != null) {
                return rd;
            }
        }
        fileName = "/WEB-INF/views/" + view + ".jspf";
        file = new File(sc.getRealPath(fileName));
        if (file.exists() && file.isFile() && file.canRead()) {
            RequestDispatcher rd = sc.getRequestDispatcher("/WEB-INF/layout/page.jsp");
            if (rd != null) {
                return rd;
            }
        }
        return null;
    }

    private static void processParameters(Context ctx, HttpServletRequest req) {
        Map<String, Object> lc = new HashMap<String, Object>(100);
        for (Map.Entry e : req.getParameterMap().entrySet()) {
            String name = (String)e.getKey();
            if (!isEmpty(name) && name.startsWith(".")) {
                name = name.substring(1); // ".filename for MIME upload
            }
            if (!isEmpty(name)) {
                Object v = e.getValue();
                if (v instanceof Object[]) {
                    Object[] a = (Object[])v;
                    if (a.length > 1) {
                        throw new Error("duplicate parameter: " + name + " in " +
                                req.toString());
                    }
                    v = a.length == 1 ? a[0] : null;
                }
                ctx.put(name, v);
                String lcn = name.toLowerCase();
                if (lc.containsKey(lcn)) {
                    throw new Error("ambiguous parameter: " + name +
                            " differs only by capitalization from " + lcn);
                }
                lc.put(lcn, v);
            }
        }
        Map<String, Field> fields = c2f.get(ctx.getClass());
        for (Map.Entry<String, Field> e : fields.entrySet()) {
            String lcn = e.getKey().toLowerCase();
            Object o = lc.get(lcn);
            if (o == null) {
                o = lc.get(lcn.replace('_', '-'));
            }
            if (o != null) {
                tryToSetField(e.getValue(), ctx, o);
            }
        }
    }

    private static void tryToSetField(Field f, Context ctx, Object v) {
        try {
            Class<?> t = f.getType();
            if (t.equals(Integer.class) || t.equals(int.class)) {
                setField(f, ctx, a2i(b2a(v)));
            } else if (t.equals(Long.class) || t.equals(long.class)) {
                setField(f, ctx, a2l(b2a(v)));
            } else if (t.equals(Double.class) || t.equals(double.class)) {
                setField(f, ctx, a2d(b2a(v)));
            } else if (t.equals(Float.class) || t.equals(float.class)) {
                setField(f, ctx, a2f(b2a(v)));
            } else if (Number.class.isAssignableFrom(f.getType()) && v instanceof Number) {
                setField(f, ctx, v);
            } else if (t.equals(String.class)) {
                setField(f, ctx, v.toString());
            } else if (t.equals(Boolean.class) || t.equals(boolean.class)) {
                setField(f, ctx, a2b(b2a(v)));
            } else if (t.equals(byte[].class) && v instanceof byte[]) {
                setField(f, ctx, v);
            } else if (t.isAssignableFrom(Date.class) && v instanceof String) {
                setField(f, ctx, s2d((String)v));
            } else if (t.isAssignableFrom(Date.class) && v instanceof Long) {
                setField(f, ctx, new Date((Long)v));
            }
        } catch (Throwable t) {
            trace("WARNING: failed to set value of " +
                  ctx.getClass().getName() + "." + f.getName() + " to " + v);
        }
    }

    private static Object getField(Field f, Object o) {
        try { return f.get(o); } catch (Throwable e) { throw new Error(e); }
    }

    private static void setField(Field f, Object o, Object v) {
        try { f.set(o, v); } catch (Throwable e) { rethrow(e); }
    }

    public static String packageOf(String className) {
        int ix = className.lastIndexOf('.');
        return ix > 0 ? className.substring(0, ix) : null;
    }

    private static <T> Constructor<T> getDeclaredConstructor(Class<T> c) {
        try {
            Constructor<T> ctor = c.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    public static <T> T newInstance(Class<T> c) {
        return newInstance(getDeclaredConstructor(c));
    }

    private static <T> T newInstance(Constructor<T> c) {
        try { return c.newInstance(); } catch (Throwable t) { throw new Error(t); }
    }

    private static Object invoke(Method m, Object o, Object... p) {
        // IllegalAccessException, InvocationTargetException
        try { return m.invoke(o, p); } catch (Throwable t) { throw new Error(t);}
    }

    private static File resolveFile(ClassLoader cl, String resource) {
        try {
            Enumeration<URL> e = cl.getResources(resource);
            if (e.hasMoreElements()) {
                URL u = e.nextElement();
                File f = new File(URLDecoder.decode(u.getPath(), "UTF-8"));
                return exists(f) ? f : null;
            }
            return null;
        } catch (IOException e1) {
            return null;
        }
    }

    private static <T> void getAllClasses(Class<T> implementing, Set<Class<T>> set) {
        Class<?> c = getCallerClass();
        String cn = c.getName();
        String cf = cn.replace('.', File.separatorChar);
        File file = resolveFile(c.getClassLoader(), cf + ".class");
        if (file != null) {
            String pn = packageOf(cn);
            File up = file.getParentFile();
            for (;;) {
                if (pn == null || !isDirectory(up.getParentFile())) {
                    break;
                }
                pn = packageOf(pn);
                up = up.getParentFile();
            }
            collectClasses(set, up.getPath().length(), up, implementing);
        }
    }

    private static <T> void collectClasses(Set<Class<T>> set, int prefix, File dir,
            Class<T> implementing) {
        try {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (isDirectory(f)) {
                        collectClasses(set, prefix, f, implementing);
                    } else if (f.getName().endsWith(".class")) {
                        String s = f.getPath().substring(prefix + 1);
                        s = s.substring(0, s.length() - ".class".length());
                        Class<?> c = forName(s.replace(File.separatorChar, '.'));
                        if (c != null) {
                            if (implementing == null ||
                                implementing.isInterface() &&
                                getAllImplementedInterfaces(c).contains(implementing) ||
                                getAllSuperClasses(c).contains(implementing)) {
                                // noinspection unchecked
                                set.add((Class<T>)c);
                            }
                        }
                    }
                }
            }
        } catch (AccessControlException e) {
            /* ignore */
        }
    }

    private static Set<Class<?>> getAllSuperClasses(Class<?> c, Set<Class<?>> s) {
        Class<?> sc = c.getSuperclass();
        if (!sc.equals(Object.class)) {
            s.add(sc);
            getAllSuperClasses(sc, s);
        }
        return s;
    }

    private static Set<Class<?>> getAllImplementedInterfaces(Class<?> c, Set<Class<?>> s) {
        if (c == null || c == Object.class) {
            return s;
        }
        for (Class<?> i : c.getInterfaces()) {
            if (i.isInterface()) {
                s.add(i);
            }
        }
        return getAllImplementedInterfaces(c.getSuperclass(), s);
    }

    private static Set<Class<?>> getAllImplementedInterfaces(Class<?> c) {
        Set<Class<?>> s = c2i.get(c);
        if (s == null) {
            s = getAllImplementedInterfaces(c, new HashSet<Class<?>>());
            c2i.put(c, s);
        }
        return s;
    }

    private static Set<Class<?>> getAllSuperClasses(Class<?> c) {
        Set<Class<?>> s = c2s.get(c);
        if (s == null) {
            s = getAllSuperClasses(c, new HashSet<Class<?>>());
            c2s.put(c, s);
        }
        return s;
    }

    private static <T> Map<String, Method> collectMethods(Set<Class<T>> cs, Map<String, Method> r) {
        for (Class<T> c : cs) {
            collectMethods(c, r);
        }
        return r;
    }

    private static <T> void collectMethods(Class<T> c, Map<String, Method> r) {
        Method[] ms = c.getMethods();
        for (Method m : ms) {
            String name = m.getName().toLowerCase();
            if (m.getParameterTypes().length == 0 && isPublic(m.getModifiers()) &&
                m.getDeclaringClass().equals(c) && void.class.equals(m.getReturnType())) {
                if (r.containsKey(name)) {
                    throw new Error("ambiguous method: " + c.getName() + "." + m.getName() +
                            " clashes with " +
                            r.get(name).getDeclaringClass().getName() + "." + r.get(name).getName());
                }
                r.put(name, m);
            }
        }
    }

    private static <T> void collectFields(Class<T> c) {
        T o = newInstance(c);  // object to test accessibility
        Map<String, Field> m = c2f.get(c);
        if (m == null) {
            m = new HashMap<String, Field>();
            collectFields(c, o, m);
            c2f.put(c, Collections.unmodifiableMap(m));
        }
    }

    private static void collectFields(Class<?> c, Object o, Map<String, Field> map) {
        if (c != Object.class && !c.isInterface()) {
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                try {
                    f.setAccessible(true);
                    int m = f.getModifiers();
                    boolean aes = f.isAccessible() && !f.isEnumConstant() && !f.isSynthetic();
                    boolean fst = isFinal(m) || isStatic(m) || isTransient(m);
                    if (aes && !fst) {
                        getField(f, o); // try to access the field
                        assert !map.containsKey(f.getName()) : "duplicate field: " + f.getName();
                        map.put(f.getName(), f);
                    }
                } catch (Throwable x) {
                    /* field is not accessible ignore it */
                }
            }
            collectFields(c.getSuperclass(), o, map);
        }
    }

}
