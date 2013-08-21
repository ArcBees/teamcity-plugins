package com.arcbees.bitbucket.model;

import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("pull_request_id")
    private Integer pullRequestId;
    @SerializedName("comment_id")
    private Long commentId;
    private boolean deleted;

    public Integer getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(Integer pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
