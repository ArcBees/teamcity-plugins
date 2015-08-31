/*
 * Copyright 2015 ArcBees Inc.
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

import java.util.List;
import java.util.Map;

import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;
import com.arcbees.vcs.model.PullRequests;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PullRequestChainParser {
    public void parsePullRequestChains(PullRequests<? extends PullRequest> pullRequests) {
        Map<String, PullRequest> pullRequestsMap = Maps.newHashMap();
        for (PullRequest pullRequest : pullRequests.getPullRequests()) {
            pullRequestsMap.put(pullRequest.getSource().getBranch().getName(), pullRequest);
        }

        for (PullRequest pullRequest : pullRequestsMap.values()) {
            List<String> chain = Lists.newArrayList();

            PullRequestTarget destination = pullRequest.getDestination();
            String branchName = destination.getBranch().getName();
            chain.add(branchName);
            do {
                PullRequest destinationPullRequest = pullRequestsMap.get(branchName);
                if (destinationPullRequest != null) {
                    branchName = destinationPullRequest.getDestination().getBranch().getName();
                    chain.add(branchName);
                } else {
                    break;
                }
            } while (pullRequestsMap.containsKey(branchName));

            pullRequest.setBranchChain(chain);
        }
    }
}
