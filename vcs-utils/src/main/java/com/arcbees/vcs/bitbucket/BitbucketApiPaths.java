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

public class BitbucketApiPaths {
    private static final String API_2 = "/api/2.0/";
    private static final String API_1 = "/api/1.0/";
    private static final String REPOSITORIES = "repositories/";
    private static final String PULLREQUESTS = "/pullrequests/";
    private static final String COMMENTS = "/comments";
    private static final String COMMIT = "/commit";
    private static final String STATUS = "/statuses/build";
    private static final String APPROVE = "/approve";
    private static final String SLASH = "/";
    private static final String STATE_MERGED = "?state=MERGED";

    private final String baseUrl;

    public BitbucketApiPaths(String baseUrl) {
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
        return baseUrl + API_2 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId);
    }
    
    public String updateStatus(String repositoryOwner,
            String repositoryName,
            String commitHash) {
        return baseUrl + API_2 + REPOSITORIES + repositoryOwner + SLASH + repositoryName + COMMIT + SLASH + commitHash + STATUS;
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

    private String getPullRequests(String repositoryOwner, String repositoryName) {
        return baseUrl + API_2 + REPOSITORIES + repositoryOwner + SLASH + repositoryName + PULLREQUESTS;
    }

    private String pathToPullRequest(String repositoryOwner, String repositoryName, Integer pullRequestId) {
        return REPOSITORIES + repositoryOwner + SLASH + repositoryName + PULLREQUESTS + pullRequestId;
    }

    public String approvePullRequest(String repositoryOwner, String repositoryName, Integer pullRequestId) {
        return baseUrl + API_2 + pathToPullRequest(repositoryOwner, repositoryName, pullRequestId) + APPROVE;
    }
}
