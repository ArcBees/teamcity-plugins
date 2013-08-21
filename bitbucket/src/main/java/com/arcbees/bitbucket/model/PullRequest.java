/*
 * Copyright 2013 ArcBees Inc.
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

package com.arcbees.bitbucket.model;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class PullRequest {
    private String status;
    private String description;
    private List<Link> links;
    private String title;
    private int id;
    @SerializedName("created_on")
    private Date createdOn;
    @SerializedName("updated_on")
    private Date updatedOn;
    private User user;
    @SerializedName("merge_commit")
    private Commit mergeCommit;
    @SerializedName("closed_by")
    private User closedBy;
    private PullRequestTarget source;
    private PullRequestTarget destination;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Commit getMergeCommit() {
        return mergeCommit;
    }

    public void setMergeCommit(Commit mergeCommit) {
        this.mergeCommit = mergeCommit;
    }

    public User getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy = closedBy;
    }

    public PullRequestTarget getSource() {
        return source;
    }

    public void setSource(PullRequestTarget source) {
        this.source = source;
    }

    public PullRequestTarget getDestination() {
        return destination;
    }

    public void setDestination(PullRequestTarget destination) {
        this.destination = destination;
    }
}
