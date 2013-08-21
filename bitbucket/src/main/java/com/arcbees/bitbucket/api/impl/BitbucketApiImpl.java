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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.model.Branch;
import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.model.PullRequestTarget;
import com.arcbees.bitbucket.model.PullRequests;
import com.arcbees.bitbucket.util.GsonDateTypeAdapter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BitbucketApiImpl implements BitbucketApi {
    private final HttpClientWrapper httpClient;
    private final Gson gson;
    private final BitbucketApiPaths apiPaths;
    private final String repositoryOwner;
    private final String repositoryName;
    private final UsernamePasswordCredentials credentials;

    public BitbucketApiImpl(HttpClientWrapper httpClient,
                            BitbucketApiPaths apiPaths,
                            String userName,
                            String password,
                            String repositoryOwner,
                            String repositoryName) {
        this.httpClient = httpClient;
        this.apiPaths = apiPaths;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.credentials = new UsernamePasswordCredentials(userName, password);
        this.gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDateTypeAdapter()).create();
    }

    @Override
    public PullRequests getOpenedPullRequests() throws IOException {
        String requestUrl = apiPaths.getOpenedPullRequests(repositoryOwner, repositoryName);

        HttpGet request = new HttpGet(requestUrl);

        return processResponse(request, PullRequests.class);
    }

    @Override
    public PullRequest getPullRequestForBranch(final String branchName) throws IOException {
        PullRequests pullRequests = getOpenedPullRequests();

        return Iterables.tryFind(pullRequests.getPullRequests(), new Predicate<PullRequest>() {
            @Override
            public boolean apply(PullRequest pullRequest) {
                PullRequestTarget source = pullRequest.getSource();
                Branch branch = source.getBranch();
                String pullRequestBranchName = branch.getName();

                return pullRequestBranchName.equals(branchName);
            }
        }).orNull();
    }

    @Override
    public void deleteComment(Integer pullRequestId, Long commentId) throws IOException {
        String requestUrl = apiPaths.deleteComment(repositoryOwner, repositoryName, pullRequestId, commentId);

        HttpDelete request = new HttpDelete(requestUrl);

        includeAuthentication(request);
        setDefaultHeaders(request);

        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to complete request to Bitbucket. Status: " + response.getStatusLine());
            }
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    @Override
    public Comment postComment(Integer pullRequestId,
                               String comment) throws IOException {
        String requestUrl = apiPaths.addComment(repositoryOwner, repositoryName, pullRequestId);

        HttpPost request = new HttpPost(requestUrl);

        List<NameValuePair> postParameters = Lists.newArrayList();
        postParameters.add(new BasicNameValuePair("content", comment));

        request.setEntity(new UrlEncodedFormEntity(postParameters));

        return processResponse(request, Comment.class);
    }

    private <T> T processResponse(HttpUriRequest request,
                                  Class<T> clazz) throws IOException {
        includeAuthentication(request);
        setDefaultHeaders(request);
        try {
            HttpResponse execute = httpClient.execute(request);
            if (execute.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to complete request to Bitbucket. Status: " + execute.getStatusLine());
            }

            HttpEntity entity = execute.getEntity();
            if (entity == null) {
                throw new IOException(
                        "Failed to complete request to Bitbucket. Empty response. Status: " + execute.getStatusLine());
            }

            try {
                return readEntity(clazz, entity);
            } finally {
                EntityUtils.consume(entity);
            }
        } finally {
            request.abort();
        }
    }

    private <T> T readEntity(Class<T> clazz, HttpEntity entity) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);
        String json = outputStream.toString(CharEncoding.UTF_8);

        return gson.fromJson(json, clazz);
    }

    private void includeAuthentication(HttpRequest request) throws IOException {
        try {
            request.addHeader(new BasicScheme().authenticate(credentials, request, null));
        } catch (AuthenticationException e) {
            throw new IOException("Failed to set authentication for request. " + e.getMessage(), e);
        }
    }

    private void setDefaultHeaders(HttpUriRequest request) {
        request.setHeader(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, CharEncoding.UTF_8));
        request.setHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()));
    }
}
