package com.arcbees.bitbucket.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.model.Branch;
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
    private final UsernamePasswordCredentials credentials;

    public BitbucketApiImpl(HttpClientWrapper httpClient,
                            BitbucketApiPaths apiPaths,
                            String userName,
                            String password) {
        this.httpClient = httpClient;
        this.apiPaths = apiPaths;
        this.credentials = new UsernamePasswordCredentials(userName, password);
        this.gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDateTypeAdapter()).create();
    }

    @Override
    public PullRequests getOpenedPullRequests(String repositoryOwner,
                                              String repositoryName) throws IOException {
        String requestUrl = apiPaths.getOpenedPullRequests(repositoryOwner, repositoryName);

        HttpGet request = new HttpGet(requestUrl);

        return processResponse(request, PullRequests.class);
    }

    @Override
    public PullRequest getPullRequestForBranch(String repositoryOwner, String repositoryName, final String branchName)
            throws IOException {
        PullRequests pullRequests = getOpenedPullRequests(repositoryOwner, repositoryName);

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
    public void postComment(String ownerName,
                            String repoName,
                            Integer pullRequestId,
                            String comment) throws IOException {

        String requestUrl = apiPaths.addComment(ownerName, repoName, pullRequestId);

        HttpPost request = new HttpPost(requestUrl);

        List<NameValuePair> postParameters = Lists.newArrayList();
        postParameters.add(new BasicNameValuePair("content", comment));

        request.setEntity(new UrlEncodedFormEntity(postParameters));

        includeAuthentication(request);
        setDefaultHeaders(request);

        HttpResponse execute = httpClient.execute(request);
        if (execute.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to complete request to Bitbucket. Status: " + execute.getStatusLine());
        }
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
        String json = outputStream.toString("utf-8");

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
        request.setHeader(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "UTF-8"));
        request.setHeader(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
    }
}
