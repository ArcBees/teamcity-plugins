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

public class StashApiPaths {
    private static final String API_1 = "/rest/api/1.0/projects/";
    private static final String REPOSITORIES = "repos/";
    private static final String PULLREQUESTS = "/pull-requests/";
    private static final String COMMENTS = "/comments";
    private static final String APPROVE = "/approve";
    private static final String SLASH = "/";
    private static final String STATE_MERGED = "?state=MERGED";

    private final String baseUrl;

    public StashApiPaths(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        this.baseUrl = baseUrl;
    }

    public String getOpenedPullRequests(String repositoryOwner,
                                        String repositoryName) {
        return getPullRequests(repositoryOwner, repositoryName);
    }

    public String getMergedPullRequests(String repositoryOwner, String repositoryName) {
        return getPullRequests(repositoryOwner, repositoryName) + STATE_MERGED;
    }

    public String getPullRequest(String repositoryOwner,
                                 String repositoryName,
                                 Integer pullRequestId) {
        return baseUrl + API_1 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId);
    }

    public String addComment(String repositoryOwner,
                             String repositoryName,
                             Integer pullRequestId) {
        return baseUrl + API_1 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId) + COMMENTS;
    }

    public String pullRequestComment(String repositoryOwner,
                                     String repositoryName,
                                     Integer pullRequestId,
                                     Long commentId) {
        return baseUrl + API_1 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId)
                + COMMENTS + SLASH + commentId;
    }

    private String getPullRequests(String repositoryOwner, String repositoryName) {
        return baseUrl + API_1 + repositoryOwner + SLASH + REPOSITORIES + repositoryName + PULLREQUESTS;
    }

    public String approvePullRequest(String repositoryOwner, String repositoryName, Integer pullRequestId) {
        return baseUrl + API_1 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId) + APPROVE;
    }

    private String pathToPullRequest(String repositoryOwner, String repositoryName, Integer pullRequestId) {
        return repositoryOwner + SLASH + REPOSITORIES + repositoryName + PULLREQUESTS + pullRequestId;
    }
}
