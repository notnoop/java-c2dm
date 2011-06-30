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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.notnoop.c2dm.C2DMDelegate;
import com.notnoop.c2dm.C2DMNotification;
import com.notnoop.c2dm.C2DMResponse;
import com.notnoop.c2dm.exceptions.RuntimeIOException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class Utilities {
    private Utilities() { throw new AssertionError("Uninstantiable class"); }

    /**
     * The default URL for Google C2DM push notifications
     */
    public static String DEFAULT_C2DM_SERVICE_URI = "https://android.apis.google.com/c2dm/send";

    public static ThreadSafeClientConnManager poolManager(int maxConnections) {
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
        cm.setMaxTotal(maxConnections);
        cm.setDefaultMaxPerRoute(maxConnections);

        return cm;
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

    private static final String UPDATE_CLIENT_AUTH = "Update-Client-Auth";

    public static void fireDelegate(C2DMNotification message,
            HttpResponse response, C2DMDelegate delegate) {
        if (delegate == null) {
            return;
        }

        List<NameValuePair> pairs;
        try {
            pairs = parseResponse(response.getEntity());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
        C2DMResponse r = logicalResponseFor(response, pairs);

        if (r == C2DMResponse.SUCCESSFUL) {
            assert pairs.size() == 1 && "id".equalsIgnoreCase(pairs.get(0).getName());
            String id = pairs.get(0).getValue();
            delegate.messageSent(message, r, id);
        } else {
            delegate.messageFailed(message, r);
        }

        if (response.containsHeader(UPDATE_CLIENT_AUTH)) {
            Header header = response.getFirstHeader(UPDATE_CLIENT_AUTH);

            String newAuthToken = header.getValue();
            delegate.authTokenUpdated(newAuthToken);
        }
    }

    private static final C2DMResponse[] logicalResponses = C2DMResponse.values();
    public static C2DMResponse logicalResponseFor(HttpResponse response, List<NameValuePair> pairs) {
        int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
        case 503: return C2DMResponse.SERVER_UNAVAILABLE;
        case 401: return C2DMResponse.INVALID_AUTHENTICATION;
        case 200: {
            assert pairs.size() == 1;

            NameValuePair entry = pairs.get(0);
            if ("id".equals(entry.getName())) {
                return C2DMResponse.SUCCESSFUL;
            }

            assert "Error".equals(entry.getName());
            String value = entry.getValue();

            for (C2DMResponse r: logicalResponses) {
                if (value.equals(r.getKey())) {
                    return r;
                }
            }

            return C2DMResponse.UNKNOWN_ERROR;
        }
        default: return C2DMResponse.UNKNOWN_ERROR;
        }
    }

    /**
     * Workaround Google responding with Content-Type being
     * "text/plain" rather than "application/x-www-form-urlencoded"
     * as expected by Apache HTTP.
     */
    public static List<NameValuePair> parseResponse(HttpEntity entity)
    throws ParseException, IOException {
        String charset = "UTF-8";
        List<NameValuePair> result = new ArrayList<NameValuePair>();
        final String content = EntityUtils.toString(entity, HTTP.ASCII);
        if (content != null && content.length() > 0) {
            result = new ArrayList <NameValuePair>();
            URLEncodedUtils.parse(result, new Scanner(content), charset);
        }
        return result;
    }
}
