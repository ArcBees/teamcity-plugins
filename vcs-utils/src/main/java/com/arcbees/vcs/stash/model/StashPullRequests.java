/**
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

import java.util.List;

import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequests;
import com.google.gson.annotations.SerializedName;

public class StashPullRequests implements PullRequests<StashPullRequest> {
    @SerializedName("values")
    private List<StashPullRequest> pullRequests;

    @Override
    public List<? extends PullRequest> getPullRequests() {
        return pullRequests;
    }

    @Override
    public void setPullRequests(List<StashPullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }
}
