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

package com.arcbees.staging;

import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.PullRequest;

public class TomcatStagingDeploy {
    private final PullRequest pullRequest;

    private Comment comment;
    private boolean deployed;
    private boolean undeployed;
    private String webPath;

    public TomcatStagingDeploy(PullRequest pullRequest, boolean deployed) {
        this(pullRequest, deployed, false);
    }

    public TomcatStagingDeploy(PullRequest pullRequest, boolean deployed, boolean undeployed) {
        this.pullRequest = pullRequest;
        this.deployed = deployed;
        this.undeployed = undeployed;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public boolean isUndeployed() {
        return undeployed;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    public void setUndeployed(boolean undeployed) {
        this.undeployed = undeployed;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public String getWebPath() {
        return webPath;
    }

    public void setWebPath(String webPath) {
        this.webPath = webPath;
    }
}
