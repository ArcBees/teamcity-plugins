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

package com.arcbees.vcs.bitbucket;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.arcbees.vcs.AbstractVcsApi;
import com.arcbees.vcs.bitbucket.model.BitbucketComment;
import com.arcbees.vcs.bitbucket.model.BitbucketPullRequests;
import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.CommitStatus;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequests;
import com.arcbees.vcs.util.GsonDateTypeAdapter;
import com.arcbees.vcs.util.HttpClientWrapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BitbucketApi extends AbstractVcsApi {
    private static final Logger LOGGER = Logger.getLogger(BitbucketApi.class.getName());
    private final HttpClientWrapper httpClient;
    private final Gson gson;
    private final BitbucketApiPaths apiPaths;
    private final String repositoryOwner;
    private final String repositoryName;
    private final UsernamePasswordCredentials credentials;
    private final boolean approveOnSuccess;

    public BitbucketApi(HttpClientWrapper httpClient,
                        BitbucketApiPaths apiPaths,
                        String userName,
                        String password,
                        String repositoryOwner,
                        String repositoryName,
                        boolean approveOnSuccess) {
        this.httpClient = httpClient;
        this.apiPaths = apiPaths;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.credentials = new UsernamePasswordCredentials(userName, password);
        this.gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDateTypeAdapter()).create();
        this.approveOnSuccess = approveOnSuccess;
    }

    @Override
    public PullRequests getOpenedPullRequests() throws IOException {
        String requestUrl = apiPaths.getOpenedPullRequests(repositoryOwner, repositoryName);

        HttpGet request = new HttpGet(requestUrl);

        return processResponse(httpClient, request, credentials, gson, BitbucketPullRequests.class);
    }

    @Override
    public PullRequests getMergedPullRequests() throws IOException {
        String requestUrl = apiPaths.getMergedPullRequests(repositoryOwner, repositoryName);

        HttpGet request = new HttpGet(requestUrl);

        return processResponse(httpClient, request, credentials, gson, BitbucketPullRequests.class);
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

        executeRequest(httpClient, request, credentials);
    }

    @Override
    public Comment postComment(Integer pullRequestId,
                               String comment) throws IOException {
        String requestUrl = apiPaths.addComment(repositoryOwner, repositoryName, pullRequestId);

        HttpPost request = new HttpPost(requestUrl);

        List<NameValuePair> postParameters = Lists.newArrayList();
        postParameters.add(new BasicNameValuePair("content", comment));

        request.setEntity(new UrlEncodedFormEntity(postParameters));

        return processResponse(httpClient, request, credentials, gson, BitbucketComment.class);
    }

    @Override
    public void updateStatus(String commitHash, String message, CommitStatus status, String targetUrl)
            throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void approvePullRequest(Integer pullRequestId) throws IOException, UnsupportedOperationException {
        if (!approveOnSuccess){
            LOGGER.log(Level.INFO, "Skipping approve pull request.");
            return;
        }

        String requestUrl = apiPaths.approvePullRequest(repositoryOwner, repositoryName, pullRequestId);

        LOGGER.log(Level.INFO, "Approve pull request url - {0}.", new Object[] {requestUrl});

        HttpPost request = new HttpPost(requestUrl);

        executeRequest(httpClient, request, credentials);
    }

    @Override
    public void deletePullRequestApproval(Integer pullRequestId) throws IOException, UnsupportedOperationException {
        String requestUrl = apiPaths.approvePullRequest(repositoryOwner, repositoryName, pullRequestId);

        LOGGER.log(Level.INFO, "Unapprove pull request url - {0}.", new Object[] {requestUrl});

        HttpDelete request = new HttpDelete(requestUrl);

        executeRequest(httpClient, request, credentials);
    }

}
