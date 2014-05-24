/**
 * Copyright 2014 ArcBees Inc.
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import jetbrains.buildServer.version.ServerVersionHolder;

public class HttpClientWrapperImpl implements HttpClientWrapper {
    private static final int RETRY_COUNT = 3;
    private static final int TIMEOUT = 30 * 1000;

    private final HttpClient httpClient;

    private PoolingHttpClientConnectionManager connectionManager;

    public HttpClientWrapperImpl() {
        httpClient = initHttpClient();
    }

    private CloseableHttpClient initHttpClient() {
        RequestConfig requestConfig = getRequestConfig();

        String serverVersion = ServerVersionHolder.getVersion().getDisplayVersion();

        connectionManager = new PoolingHttpClientConnectionManager();

        return HttpClientBuilder.create()
                .useSystemProperties()
                .addInterceptorFirst(new RequestAcceptEncoding())
                .addInterceptorFirst(new ResponseContentEncoding())
                .setRetryHandler(new DefaultHttpRequestRetryHandler(RETRY_COUNT, true))
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent("JetBrains TeamCity " + serverVersion)
                .build();
    }

    private RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .build();
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
        connectionManager.shutdown();
    }
}
