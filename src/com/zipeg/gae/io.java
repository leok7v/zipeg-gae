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

import java.io.*;
import java.security.*;

import static com.zipeg.gae.util.*;

/** @noinspection UnusedDeclaration */
public class io {

    // include file prefix. content of file is treated as a list of other filenames to combine
    private static final char INCLUDE = '_';

    public static boolean exists(File f) {
        try {
            return f.exists();
        } catch (AccessControlException x) {
            return false;
        }
    }

    public static boolean isDirectory(File f) {
        try {
            return f.isDirectory();
        } catch (AccessControlException x) {
            return false;
        }
    }

    public static boolean isIncludeFile(File f) {
        String n = f.getName();
        return n.length() > 0 && n.charAt(0) == INCLUDE;
    }

    public static File makeIncludeFile(File f) {
        return new File(f.getParent(), INCLUDE + f.getName());
    }

    public static byte[] readFully(File f) {
        if (isIncludeFile(f)) {
            return combine(f);
        } else {
            return readFileContentFully(f);
        }
    }

    private static byte[] readFileContentFully(File f) {
        try {
            return readFullyAndClose(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            throw new Error(e);
        }
    }

    public static void close(InputStream s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            rethrow(e);
        }
    }

    public static String getMimeTypeFromFilename(String name) {
        name = name.toLowerCase();
        if (name.equals("update.txt")) return "application/octet-stream";
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".ico")) return "image/x-icon";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".jpg")) return "image/jpeg";
        if (name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".htm")) return "text/html";
        if (name.endsWith(".xhtml")) return "application/xhtml+xml";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".dmg")) return "application/x-apple-diskimage";
        if (name.endsWith(".exe")) return "application/octet-stream";
        return "text/plain";
    }

    private static byte[] combine(File f) {
        String s = new String(readFileContentFully(f));
        String[] lines = s.split("\\r?\\n");
        int total = 0;
        for (String line : lines) {
            String t = line.trim();
            if (t.length() > 0 && !t.startsWith("#")) {
                File i = new File(f.getParentFile(), t);
                if (i.exists()) {
                    total += i.length();
/*
                    stdio.err.println(i + " last modified:" + new Date(i.lastModified()));
*/
                } else {
                    throw new Error("file " + i + " not found.");
                }
            }
        }
        byte[] content = new byte[total];
        int offset = 0;
        for (String line : lines) {
            String t = line.trim();
            if (t.length() > 0 && !t.startsWith("#")) {
                File i = new File(f.getParentFile(), t);
                byte[] buf = readFileContentFully(i);
                System.arraycopy(buf, 0, content, offset, buf.length);
                offset += buf.length;
            }
        }
        return content;
    }

    public static long lastModified(File f) {
        if (!isIncludeFile(f)) {
            return f.lastModified();
        } else {
            String s = new String(readFileContentFully(f));
            String[] lines = s.split("\\r?\\n");
            long lastModified = f.lastModified();
            for (String line : lines) {
                String t = line.trim();
                if (t.length() > 0 && !t.startsWith("#")) {
                    File i = new File(f.getParentFile(), t);
                    if (i.exists()) {
                        lastModified = Math.max(lastModified, i.lastModified());
                    } else {
                        throw new Error("file " + i + " not found.");
                    }
                }
            }
            return lastModified;
        }
    }

    public static byte[] readFully(InputStream is) {
        try {
            int len = Math.max(4096, is.available());
            ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
            byte[] buf = new byte[len];
            for (;;) {
                int k = is.read(buf);
                if (k <= 0) {
                    return baos.toByteArray();
                }
                baos.write(buf, 0, k);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public static byte[] readFullyAndClose(InputStream is) {
        try {
            return readFully(is);
        } finally {
            close(is);
        }
    }

}
