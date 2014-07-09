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

package com.arcbees.vcs.bitbucket.model;

import java.util.List;

import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequests;
import com.google.gson.annotations.SerializedName;

public class BitbucketPullRequests implements PullRequests<BitbucketPullRequest> {
    @SerializedName("values")
    private List<BitbucketPullRequest> pullRequests;

    @Override
    public List<? extends PullRequest> getPullRequests() {
        return pullRequests;
    }

    @Override
    public void setPullRequests(List<BitbucketPullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }
}
