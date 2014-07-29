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

package com.arcbees.vcs.github;

public class GitHubApiPaths {
    private static final String REPOSITORIES = "/repos/";
    private static final String PULLREQUESTS = "/pulls";
    private static final String COMMENTS = "/comments";
    private static final String SLASH = "/";
    private static final String STATE_MERGED = "?state=closed";
    private static final String STATUSES = "/statuses/";

    private final String baseUrl;

    public GitHubApiPaths(String baseUrl) {
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
        return baseUrl + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId);
    }

    public String addComment(String repositoryOwner,
                             String repositoryName,
                             Integer pullRequestId) {
        return baseUrl + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId) + COMMENTS;
    }

    public String deleteComment(String repositoryOwner,
                                String repositoryName,
                                Integer pullRequestId,
                                Long commentId) {
        return baseUrl + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId)
                + COMMENTS + SLASH + commentId;
    }

    public String updateStatus(String repositoryOwner, String repositoryName, String commitHash) {
        return baseUrl + REPOSITORIES + repositoryOwner + SLASH + repositoryName + STATUSES + commitHash;
    }

    private String getPullRequests(String repositoryOwner, String repositoryName) {
        return baseUrl + REPOSITORIES + repositoryOwner + SLASH + repositoryName + PULLREQUESTS;
    }

    private String pathToPullRequest(String repositoryOwner, String repositoryName, Integer pullRequestId) {
        return REPOSITORIES + repositoryOwner + SLASH + repositoryName + PULLREQUESTS + SLASH + pullRequestId;
    }
}
