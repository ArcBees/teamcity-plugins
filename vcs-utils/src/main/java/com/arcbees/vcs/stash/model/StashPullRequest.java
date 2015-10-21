/*
 * Copyright 2014 ArcBees Inc.
 *
 * This file is part of Stash TeamCity plugin.
 *
 * Stash TeamCity plugin is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Stash TeamCity plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with Stash TeamCity plugin. If not,
 * see http://www.gnu.org/licenses/.
 */

package com.arcbees.vcs.stash.model;

import java.util.Date;
import java.util.List;

import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

public class StashPullRequest implements PullRequest<StashPullRequestTarget> {
    private String state;
    private String description;
    private String title;
    private int id;
    private Date createdDate;
    private Date updatedDate;
    private StashPullRequestTarget fromRef;
    private StashPullRequestTarget toRef;
    @Expose(serialize = false, deserialize = false)
    private List<String> branchChain = Lists.newArrayList();

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
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Date getCreatedOn() {
        return createdDate;
    }

    @Override
    public void setCreatedOn(Date createdOn) {
        this.createdDate = createdOn;
    }

    @Override
    public Date getUpdatedOn() {
        return updatedDate;
    }

    @Override
    public void setUpdatedOn(Date updatedOn) {
        this.updatedDate = updatedOn;
    }

    @Override
    public PullRequestTarget getSource() {
        return fromRef;
    }

    @Override
    public void setSource(StashPullRequestTarget source) {
        this.fromRef = source;
    }

    @Override
    public PullRequestTarget getDestination() {
        return toRef;
    }

    @Override
    public void setDestination(StashPullRequestTarget destination) {
        this.toRef = destination;
    }

    @Override
    public List<String> getBranchChain() {
        return branchChain;
    }

    @Override
    public void setBranchChain(List<String> chain) {
        branchChain.clear();
        if (chain != null) {
            branchChain.addAll(chain);
        }
    }
}
