/**
 * Copyright 2014 ArcBees Inc.
 *
 * This file is part of Stash TeamCity plugin.
 *
 * Stash TeamCity plugin is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Stash TeamCity plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with Stash TeamCity plugin. If not,
 * see http://www.gnu.org/licenses/.
 */

package com.arcbees.vcs.stash;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.arcbees.vcs.AbstractVcsApi;
import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequests;
import com.arcbees.vcs.stash.model.StashComment;
import com.arcbees.vcs.stash.model.StashPullRequests;
import com.arcbees.vcs.util.GsonDateTypeAdapter;
import com.arcbees.vcs.util.HttpClientWrapper;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StashApiImpl extends AbstractVcsApi {
    private final HttpClientWrapper httpClient;
    private final Gson gson;
    private final StashApiPaths apiPaths;
    private final String repositoryOwner;
    private final String repositoryName;
    private final UsernamePasswordCredentials credentials;

    public StashApiImpl(HttpClientWrapper httpClient,
                        StashApiPaths apiPaths,
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

        return processResponse(httpClient, request, credentials, gson, StashPullRequests.class);
    }

    @Override
    public PullRequests getMergedPullRequests() throws IOException {
        String requestUrl = apiPaths.getMergedPullRequests(repositoryOwner, repositoryName);

        HttpGet request = new HttpGet(requestUrl);

        return processResponse(httpClient, request, credentials, gson, StashPullRequests.class);
    }

    @Override
    public PullRequest getPullRequestForBranch(final String branchName) throws IOException {
        PullRequest pullRequestForBranch = findPullRequestForBranch(branchName, getOpenedPullRequests());

        if (pullRequestForBranch == null) {
            pullRequestForBranch = findPullRequestForBranch(branchName, getMergedPullRequests());
        }

        return pullRequestForBranch;
    }

    @Override
    public void deleteComment(Integer pullRequestId, Long commentId) throws IOException {
        String requestUrl = apiPaths.deleteComment(repositoryOwner, repositoryName, pullRequestId, commentId);

        HttpDelete request = new HttpDelete(requestUrl);

        includeAuthentication(request, credentials);
        setDefaultHeaders(request);

        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to complete request to Stash. Status: " + response.getStatusLine());
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
        request.setHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()));
        request.setEntity(new ByteArrayEntity(gson.toJson(new StashComment(comment)).getBytes(Charsets.UTF_8)));

        return processResponse(httpClient, request, credentials, gson, StashComment.class);
    }
}
