package com.arcbees.bitbucket.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("username")
    private String userName;
    @SerializedName("display_name")
    private String displayName;
    private List<Link> links;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
