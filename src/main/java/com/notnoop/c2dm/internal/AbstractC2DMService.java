/*
 * Copyright 2011, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
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

import java.io.UnsupportedEncodingException;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import com.notnoop.c2dm.C2DMNotification;
import com.notnoop.c2dm.C2DMService;
import com.notnoop.c2dm.exceptions.NetworkIOException;

public abstract class AbstractC2DMService implements C2DMService {
    private final String serviceUri;
    private final String authToken;

    protected AbstractC2DMService(String serviceUri, String authToken) {
        this.serviceUri = serviceUri;
        this.authToken = authToken;
    }

    protected HttpPost postMessage(String registrationId, C2DMNotification notification) {
        HttpPost method = new HttpPost(serviceUri);
        try {
            method.setEntity(new UrlEncodedFormEntity(
                    Utilities.requestBodyOf(registrationId, notification),
                    "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("No UTF-8! It's Doom Day!");
        }

        method.addHeader("Authorization", "GoogleLogin auth=" + authToken);

        return method;
    }

    protected abstract void push(HttpPost request, C2DMNotification message);

    public void push(String registrationId, String payload)
            throws NetworkIOException {
        throw new RuntimeException("Not implemented yet");
    }

    public void push(String registrationId, C2DMNotification message)
            throws NetworkIOException {
        this.push(postMessage(registrationId, message), message);
    }

    public void start() {}

    public void stop() {}
}
