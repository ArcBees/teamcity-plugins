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

package com.arcbees.vcs;

import java.io.IOException;

import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.CommitStatus;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequests;

import jetbrains.buildServer.serverSide.SRunningBuild;

public interface VcsApi {
    PullRequests getOpenedPullRequests() throws IOException;

    PullRequests getMergedPullRequests() throws IOException;

    Comment postComment(Integer pullRequestId,
                        String comment) throws IOException;

    PullRequest getPullRequestForBranch(String branchName) throws IOException;

    void deleteComment(Integer pullRequestId, Long commentId) throws IOException;

    void updateStatus(String commitHash, String message, CommitStatus status, String targetUrl, SRunningBuild build)
            throws IOException, UnsupportedOperationException;

    void approvePullRequest(Integer pullRequestId) throws IOException, UnsupportedOperationException;

    void deletePullRequestApproval(Integer pullRequestId) throws IOException, UnsupportedOperationException;
}
