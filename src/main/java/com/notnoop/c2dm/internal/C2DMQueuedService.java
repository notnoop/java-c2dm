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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.client.methods.HttpPost;

import com.notnoop.c2dm.C2DMNotification;
import com.notnoop.c2dm.C2DMService;

public class C2DMQueuedService extends AbstractC2DMService implements C2DMService {

    private AbstractC2DMService service;
    private BlockingQueue<Pair<HttpPost, C2DMNotification>> queue;
    private AtomicBoolean started = new AtomicBoolean(false);

    public C2DMQueuedService(AbstractC2DMService service, String serviceUri, String authToken) {
        super(serviceUri, authToken);
        this.service = service;
        this.queue = new LinkedBlockingQueue<Pair<HttpPost, C2DMNotification>>();
    }

    @Override
    protected void push(final HttpPost request, C2DMNotification message) {
        if (!started.get()) {
            throw new IllegalStateException("Service hans't been started or was closed");
        }

        queue.add(Pair.of(request, message));
    }

    private Thread thread;
    private volatile boolean shouldContinue;

    public void start() {
        if (started.getAndSet(true)) {
            // Should we throw a runtime IllegalStateException here?
            return;
        }

        service.start();
        shouldContinue = true;
        thread = new Thread() {
            public void run() {
                while (shouldContinue) {
                    try {
                        Pair<HttpPost, C2DMNotification> post = queue.take();
                        service.push(post.getKey(), post.getValue());
                    } catch (InterruptedException e) {}
                }
            }
        };
        thread.start();
    }


    @Override
    public void stop() {
        started.set(false);
        shouldContinue = false;
        thread.interrupt();
        service.stop();
    }

}
