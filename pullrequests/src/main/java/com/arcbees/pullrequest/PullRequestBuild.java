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

package com.arcbees.pullrequest;

import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.Commit;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;

import jetbrains.buildServer.messages.Status;

public class PullRequestBuild {
    private final PullRequest pullRequest;
    private final Status lastStatus;
    private final Comment lastComment;

    public PullRequestBuild(PullRequest pullRequest, Status lastStatus, Comment lastComment) {
        this.pullRequest = pullRequest;
        this.lastStatus = lastStatus;
        this.lastComment = lastComment;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public Status getLastStatus() {
        return lastStatus;
    }

    public String getLastCommitHash() {
        PullRequestTarget source = pullRequest.getSource();
        Commit commit = source.getCommit();

        return commit.getHash();
    }

    public Comment getLastComment() {
        return lastComment;
    }
}
