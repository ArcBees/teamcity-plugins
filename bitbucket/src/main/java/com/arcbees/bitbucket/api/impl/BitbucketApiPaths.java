package com.arcbees.bitbucket.api.impl;

public class BitbucketApiPaths {
    private static final String API_2 = "/api/2.0/";
    private static final String API_1 = "/api/1.0/";
    private static final String REPOSITORIES = "repositories/";
    private static final String PULLREQUESTS = "/pullrequests/";
    private static final String COMMENTS = "/comments";
    private static final String SLASH = "/";

    private final String baseUrl;

    public BitbucketApiPaths(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        this.baseUrl = baseUrl;
    }

    public String getOpenedPullRequests(String repositoryOwner,
                                        String repositoryName) {
        return baseUrl + API_2 + REPOSITORIES + repositoryOwner + SLASH + repositoryName + PULLREQUESTS;
    }

    public String getPullRequest(String repositoryOwner,
                                 String repositoryName,
                                 Integer pullRequestId) {
        return baseUrl + API_2 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId);
    }

    public String addComment(String repositoryOwner,
                             String repositoryName,
                             Integer pullRequestId) {
        return baseUrl + API_1 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId) + COMMENTS;
    }

    public String deleteComment(String repositoryOwner,
                                String repositoryName,
                                Integer pullRequestId,
                                Long commentId) {
        return baseUrl + API_1 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId)
                + COMMENTS + SLASH + commentId;
    }

    private String pathToPullRequest(String repositoryOwner, String repositoryName, Integer pullRequestId) {
        return REPOSITORIES + repositoryOwner + SLASH + repositoryName + PULLREQUESTS + pullRequestId;
    }
}
