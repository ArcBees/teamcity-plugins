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

package com.arcbees.vcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.codec.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.arcbees.vcs.model.Branch;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;
import com.arcbees.vcs.model.PullRequests;
import com.arcbees.vcs.util.HttpClientWrapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;

public abstract class AbstractVcsApi implements VcsApi {
    protected <T> T processResponse(HttpClientWrapper httpClient,
                                    HttpUriRequest request,
                                    Credentials credentials,
                                    Gson gson,
                                    Class<T> clazz) throws IOException {
        includeAuthentication(request, credentials);
        setDefaultHeaders(request);
        try {
            HttpResponse execute = httpClient.execute(request);
            int statusCode = execute.getStatusLine().getStatusCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                throw new IOException("Failed to complete request. Status: " + execute.getStatusLine());
            }

            HttpEntity entity = execute.getEntity();
            if (entity == null) {
                throw new IOException(
                        "Failed to complete request. Empty response. Status: " + execute.getStatusLine());
            }

            try {
                return readEntity(clazz, entity, gson);
            } finally {
                EntityUtils.consumeQuietly(entity);
            }
        } finally {
            request.abort();
        }
    }

    protected <T> T readEntity(Class<T> clazz, HttpEntity entity, Gson gson) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);
        String json = outputStream.toString(CharEncoding.UTF_8);

        return gson.fromJson(json, clazz);
    }

    protected void includeAuthentication(HttpRequest request,
                                         Credentials credentials) throws IOException {
        try {
            request.addHeader(new BasicScheme().authenticate(credentials, request, null));
        } catch (AuthenticationException e) {
            throw new IOException("Failed to set authentication for request. " + e.getMessage(), e);
        }
    }

    protected void setDefaultHeaders(HttpUriRequest request) {
        request.setHeader(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, CharEncoding.UTF_8));
        request.setHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()));
    }

    protected PullRequest findPullRequestForBranch(final String branchName, PullRequests pullRequests) {
        return (PullRequest) Iterables.tryFind(pullRequests.getPullRequests(), new Predicate<PullRequest>() {
            @Override
            public boolean apply(PullRequest pullRequest) {
                PullRequestTarget source = pullRequest.getSource();
                Branch branch = source.getBranch();
                String pullRequestBranchName = branch.getName();

                return pullRequestBranchName.equals(branchName);
            }
        }).orNull();
    }
}
