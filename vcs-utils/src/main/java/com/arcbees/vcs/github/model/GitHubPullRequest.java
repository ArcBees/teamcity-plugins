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

package com.arcbees.vcs.github.model;

import java.util.Date;

import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;
import com.google.gson.annotations.SerializedName;

public class GitHubPullRequest implements PullRequest<GitHubPullRequestTarget> {
    private String state;
    private String body;
    private String title;
    private int number;
    @SerializedName("created_at")
    private Date createdOn;
    @SerializedName("updated_at")
    private Date updatedOn;
    private GitHubPullRequestTarget head;
    private GitHubPullRequestTarget base;

    @Override
    public String getStatus() {
        return state;
    }

    @Override
    public void setStatus(String status) {
        this.state = status;
    }

    @Override
    public String getDescription() {
        return body;
    }

    @Override
    public void setDescription(String description) {
        this.body = description;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getId() {
        return number;
    }

    @Override
    public void setId(int id) {
        this.number = id;
    }

    @Override
    public Date getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public Date getUpdatedOn() {
        return updatedOn;
    }

    @Override
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public PullRequestTarget getSource() {
        return head;
    }

    @Override
    public void setSource(GitHubPullRequestTarget source) {
        this.head = source;
    }

    @Override
    public PullRequestTarget getDestination() {
        return base;
    }

    @Override
    public void setDestination(GitHubPullRequestTarget destination) {
        this.base = destination;
    }
}
