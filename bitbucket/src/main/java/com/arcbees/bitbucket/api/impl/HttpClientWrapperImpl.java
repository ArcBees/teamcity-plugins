/*
 * Copyright 2013 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.arcbees.bitbucket.api.impl;

import java.io.IOException;
import java.net.ProxySelector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import jetbrains.buildServer.version.ServerVersionHolder;

public class HttpClientWrapperImpl implements HttpClientWrapper {
    private static final int RETRY_COUNT = 3;
    private static final int TIMEOUT = 30 * 1000;

    private final HttpClient httpClient;

    public HttpClientWrapperImpl() {
        httpClient = initHttpClient();
    }

    private DefaultHttpClient initHttpClient() {
        HttpParams httpParams = getHttpParams();

        DefaultHttpClient httpclient = new DefaultHttpClient(new PoolingClientConnectionManager(), httpParams);
        httpclient.setRoutePlanner(new ProxySelectorRoutePlanner(httpclient.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault()));
        httpclient.addRequestInterceptor(new RequestAcceptEncoding());
        httpclient.addResponseInterceptor(new ResponseContentEncoding());
        httpclient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(RETRY_COUNT, true));

        return httpclient;
    }

    private HttpParams getHttpParams() {
        HttpParams httpParams = new BasicHttpParams();

        DefaultHttpClient.setDefaultHttpParams(httpParams);
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);

        String serverVersion = ServerVersionHolder.getVersion().getDisplayVersion();
        HttpProtocolParams.setUserAgent(httpParams, "JetBrains TeamCity " + serverVersion);

        return httpParams;
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException {
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            request.abort();
            throw e;
        }
    }

    public void shutdown() {
        httpClient.getConnectionManager().shutdown();
    }
}
