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
package com.notnoop.c2dm;

import java.util.ArrayList;
import java.util.List;

import com.notnoop.c2dm.internal.Pair;

/**
 * Represents a builder for constructing the notifications requests,
 * as specified by
 * <a href="http://code.google.com/android/c2dm/index.html#server">Android
 * Cloud to Device Messaging Framework documentation</a>:
 *
 */
public class C2DMNotificationBuilder {
    private String collapseKey;
    private boolean delayWhileIdle;
    private List<Pair<String, String>> data = new ArrayList<Pair<String, String>>();

    public C2DMNotificationBuilder() {}

    /**
     * Sets the collapse key for the notification.
     *
     * An arbitrary string that is used to collapse a group of like messages when the device is offline, so that only the last message gets sent to the client. This is intended to avoid sending too many messages to the phone when it comes back online. Note that since there is no guarantee of the order in which messages get sent, the "last" message may not actually be the last message sent by the application server.
     *
     * The field is required
     *
     * @return  this
     */
    public C2DMNotificationBuilder collapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
        return this;
    }

    /**
     * Sets the delay while idle flag for the message.
     *
     * indicates that the message should not be sent immediately if the device is idle. The server will wait for the device to become active, and then only the last message for each collapse_key value will be sent.
     *
     * Default value is false.
     *
     * @return  this
     */
    public C2DMNotificationBuilder delayWhileIdle(boolean delayWhileIdle) {
        this.delayWhileIdle = delayWhileIdle;
        return this;
    }

    /**
     * Appends an application specific payload data entry.
     *
     * Payload data, expressed as key-value pairs. If present, it will be included in the Intent as application data, with the <key>. There is no limit on the number of key/value pairs, though there is a limit on the total size of the message.
     *
     * This field is optional.
     *
     * @return  this
     */
    public C2DMNotificationBuilder data(String name, String value) {
        data.add(Pair.of("data." + name, value));
        return this;
    }

    private void checkInitialization() {
        if (collapseKey == null) {
            throw new IllegalStateException("Collapse Key is required and missing");
        }
    }

    /**
     * Returns a fully initialized notification object
     */
    public C2DMNotification build() {
        checkInitialization();
        return new C2DMNotification(collapseKey, delayWhileIdle, data);
    }
}
