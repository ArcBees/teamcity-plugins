package com.arcbees.bitbucket.api;

import java.io.IOException;

import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.model.PullRequests;

public interface BitbucketApi {
    PullRequests getOpenedPullRequests(String repositoryOwner,
                                       String repositoryName) throws IOException;

    void postComment(String repositoryOwner,
                     String repositoryName,
                     Integer pullRequestId,
                     String comment) throws IOException;

    PullRequest getPullRequestForBranch(String repositoryOwner, String repositoryName, String branchName)
            throws IOException;
}
