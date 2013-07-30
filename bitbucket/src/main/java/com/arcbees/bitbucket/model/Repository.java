package com.arcbees.bitbucket.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Repository {
    @SerializedName("full_name")
    private String fullName;
    private List<Link> links;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
