package com.arcbees.bitbucket.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Commit {
    @SerializedName("sha")
    private String hash;
    private List<Link> links;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
