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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.notnoop.c2dm.internal.*;

/**
 * The class is used to create instances of {@link C2DMService}.
 *
 * Note that this class is not synchronized.  If multiple threads access a
 * {@code C2DMServiceBuilder} instance concurrently, and at least on of the
 * threads modifies one of the attributes structurally, it must be
 * synchronized externally.
 *
 * Starting a new {@code C2DMService} is easy:
 *
 * <pre>
 *   C2DMService service = C2DM.newService()
 *                  .authToken("authtoken")
 *                  .build()
 * </pre>
 */
public class C2DMServiceBuilder {
    private String serviceUri = Utilities.DEFAULT_C2DM_SERVICE_URI;

    private String authToken;

    private int pooledMax = 1;
    private ExecutorService executor = null;

    private boolean isQueued = false;
    private HttpHost proxy = null;
    private HttpClient httpClient = null;
    private int timeout = -1;

    private C2DMDelegate delegate;

    /**
     * Constructs a new instance of {@code C2DMServiceBuilder}
     */
    public C2DMServiceBuilder() { }

    /**
     * Specify the address of the HTTP proxy the connection should
     * use.
     *
     * <p>Read the <a href="http://java.sun.com/javase/6/docs/technotes/guides/net/proxies.html">
     * Java Networking and Proxies</a> guide to understand the
     * proxies complexity.
     *
     * @param host  the hostname of the HTTP proxy
     * @param port  the port of the HTTP proxy server
     * @return  this
     */
    public C2DMServiceBuilder withHttpProxy(String host, int port) {
        this.proxy = new HttpHost(host, port);
        return this;
    }

    /**
     * Specify the service URI to post Google C2DM request to.
     *
     * This method should be used only for testing.  By default, the
     * notifications are posted to
     * {@linkplain https://android.apis.google.com/c2dm/send}, as specified
     * by the Google C2DM.
     */
    public C2DMServiceBuilder withServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
        return this;
    }

    public C2DMServiceBuilder withAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    // TODO: Support Proxy again
//    /**
//     * Specify the proxy to be used to establish the connections
//     * to Google Servers
//     *
//     * <p>Read the <a href="http://java.sun.com/javase/6/docs/technotes/guides/net/proxies.html">
//     * Java Networking and Proxies</a> guide to understand the
//     * proxies complexity.
//     *
//     * @param proxy the proxy object to be used to create connections
//     * @return  this
//     */
//    public C2DMServiceBuilder withProxy(Proxy proxy) {
//        this.proxy = proxy;
//        return this;
//    }

    /**
     * Sets the HttpClient instance along with any configuration
     *
     * NOTE: This is an advanced option that should be probably be used as a
     * last resort.
     *
     * @param httpClient    the httpClient to be used
     * @return this
     */
    public C2DMServiceBuilder withHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Constructs a pool of connections to the notification servers.
     *
     */
    public C2DMServiceBuilder asPool(int maxConnections) {
        return asPool(Executors.newFixedThreadPool(maxConnections), maxConnections);
    }

    /**
     * Constructs a pool of connections to the notification servers.
     *
     * Note: The maxConnections here is used as a hint to how many connections
     * get created.
     */
    public C2DMServiceBuilder asPool(ExecutorService executor, int maxConnections) {
        this.pooledMax = maxConnections;
        this.executor = executor;
        return this;
    }

    /**
     * Constructs a new thread with a processing queue to process
     * notification requests.
     *
     * @return  this
     */
    public C2DMServiceBuilder asQueued() {
        this.isQueued = true;
        return this;
    }

    /**
     * Sets the timeout for the connection
     *
     * @param   timeout     the time out period in millis
     * @return this
     */
    public C2DMServiceBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public C2DMServiceBuilder delegate(C2DMDelegate delegate) {
        this.delegate = delegate;
        return this;
    }

    /**
     * Returns a fully initialized instance of {@link C2DMService},
     * according to the requested settings.
     *
     * @return  a new instance of C2DMService
     */
    public C2DMService build() {
        checkInitialization();

        // Client Configuration
        HttpClient client;
        if (httpClient != null) {
            client = httpClient;
        } else if (pooledMax == 1) {
            client = new DefaultHttpClient();
        } else {
            client = new DefaultHttpClient(Utilities.poolManager(pooledMax));
        }

        if (proxy != null) {
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        if (timeout > 0) {
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, timeout);
            HttpConnectionParams.setSoTimeout(params, timeout);
        }

        // Configure service
        AbstractC2DMService service;
        if (pooledMax == 1) {
            service = new C2DMServiceImpl(client, serviceUri, authToken, delegate);
        } else {
            service = new C2DMPooledService(client, serviceUri, authToken, executor, delegate);
        }

        if (isQueued) {
            service = new C2DMQueuedService(service, serviceUri, authToken);
        }

        service.start();
        return service;
    }

    private void checkInitialization() {
        if (authToken == null) {
            throw new IllegalStateException("AuthToken is required");
        }
        if (pooledMax != 1 && executor == null) {
            throw new IllegalStateException("Executor service is required for pooled connections");
        }
    }
}
