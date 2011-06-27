/*
* Copyright 2011, Mahmood Ali.
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* * Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
* * Redistributions in binary form must reproduce the above
* copyright notice, this list of conditions and the following disclaimer
* in the documentation and/or other materials provided with the
* distribution.
* * Neither the name of Mahmood Ali. nor the names of its
* contributors may be used to endorse or promote products derived from
* this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
* A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
* OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
* DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
* THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.notnoop.c2dm.internal;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;

import com.notnoop.c2dm.C2DMDelegate;
import com.notnoop.c2dm.C2DMNotification;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Utilities {
    private Utilities() { throw new AssertionError("Uninstantiable class"); }

    /**
     * The default URL for Google C2DM push notifications
     */
    public static String DEFAULT_C2DM_SERVICE_URI = "https://android.apis.google.com/c2dm/send";

    public static String readFully(InputStream is) {
        final char[] buffer = new char[0x10000];
        StringBuilder out = new StringBuilder();
        try {
            Reader in = new InputStreamReader(is, "UTF-8");
            int read;
            do {
                read = in.read(buffer, 0, buffer.length);
                if (read>0) {
                    out.append(buffer, 0, read);
                }
            } while (read>=0);
            return out.toString();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ThreadSafeClientConnManager poolManager(int maxConnections) {
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
        cm.setMaxTotal(maxConnections);
        cm.setDefaultMaxPerRoute(maxConnections);

        return cm;
    }

    public static byte[] toUTF8(String content) {
        try {
            return content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("The world is coming to an end!  No UTF-8 support");
        }
    }


    public static List<NameValuePair> requestBodyOf(String registrationId, C2DMNotification notify) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();

        pairs.add(new BasicNameValuePair("registration_id", registrationId));
        pairs.add(new BasicNameValuePair("collapse_key", notify.getCollapseKey()));

        if (notify.isDelayWhileIdle()) {
            pairs.add(new BasicNameValuePair("delay_while_idle", "1"));
        }

        for (Map.Entry<String, String> data: notify.getData()) {
            pairs.add(new BasicNameValuePair(data.getKey(), data.getValue()));
        }

        return pairs;
    }

    public static void fireDelegate(C2DMNotification message,
            HttpResponse response, C2DMDelegate delegate) {
        // FIXME: Implement this method
        delegate.messageSent(message, null);
    }
}
