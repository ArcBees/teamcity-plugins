package com.arcbees.bitbucket.util;

import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.Commit;
import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.model.PullRequestTarget;

import jetbrains.buildServer.messages.Status;

public class BitbucketPullRequestBuild {
    private final PullRequest pullRequest;
    private final Status lastStatus;
    private final Comment lastComment;

    public BitbucketPullRequestBuild(PullRequest pullRequest, Status lastStatus, Comment lastComment) {
        this.pullRequest = pullRequest;
        this.lastStatus = lastStatus;
        this.lastComment = lastComment;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public Status getLastStatus() {
        return lastStatus;
    }

    public String getLastCommitHash() {
        PullRequestTarget source = pullRequest.getSource();
        Commit commit = source.getCommit();

        return commit.getHash();
    }

    public Comment getLastComment() {
        return lastComment;
    }
}
