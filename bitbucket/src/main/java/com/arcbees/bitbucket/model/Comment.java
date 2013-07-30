package com.arcbees.bitbucket.model;

public class Comment {
    private String content;

    Comment() {
    }

    public Comment(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
