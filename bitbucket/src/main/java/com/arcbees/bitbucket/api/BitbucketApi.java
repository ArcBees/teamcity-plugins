package com.arcbees.bitbucket.api;

import java.io.IOException;

import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.model.PullRequests;

public interface BitbucketApi {
    PullRequests getOpenedPullRequests() throws IOException;

    Comment postComment(Integer pullRequestId,
                        String comment) throws IOException;

    PullRequest getPullRequestForBranch(String branchName) throws IOException;

    void deleteComment(Integer pullRequestId, Long commentId) throws IOException;
}
