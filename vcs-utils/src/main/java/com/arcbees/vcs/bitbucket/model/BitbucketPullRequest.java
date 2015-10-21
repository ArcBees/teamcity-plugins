/*
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

package com.arcbees.vcs.bitbucket.model;

import java.util.Date;
import java.util.List;

import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BitbucketPullRequest implements PullRequest<BitbucketPullRequestTarget> {
    private String status;
    private String description;
    private String title;
    private int id;
    @SerializedName("created_on")
    private Date createdOn;
    @SerializedName("updated_on")
    private Date updatedOn;
    private BitbucketPullRequestTarget source;
    private BitbucketPullRequestTarget destination;
    @Expose(serialize = false, deserialize = false)
    private List<String> branchChain = Lists.newArrayList();

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
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
        return source;
    }

    @Override
    public void setSource(BitbucketPullRequestTarget source) {
        this.source = source;
    }

    @Override
    public PullRequestTarget getDestination() {
        return destination;
    }

    @Override
    public void setDestination(BitbucketPullRequestTarget destination) {
        this.destination = destination;
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
