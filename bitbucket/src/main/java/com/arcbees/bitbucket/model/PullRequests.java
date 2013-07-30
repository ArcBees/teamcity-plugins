package com.arcbees.bitbucket.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class PullRequests {
    @SerializedName("pagelen")
    private int pageLength;
    private int page;
    private int size;
    @SerializedName("values")
    private List<PullRequest> pullRequests;

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public void setPullRequests(List<PullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }

    public int getPageLength() {
        return pageLength;
    }

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
