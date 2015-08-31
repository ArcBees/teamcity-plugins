/**
 * Copyright 2015 ArcBees Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.arcbees.pullrequest;

import org.junit.Before;
import org.junit.Test;

import com.arcbees.vcs.bitbucket.model.BitbucketBranch;
import com.arcbees.vcs.bitbucket.model.BitbucketPullRequest;
import com.arcbees.vcs.bitbucket.model.BitbucketPullRequestTarget;
import com.arcbees.vcs.bitbucket.model.BitbucketPullRequests;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PullRequestChainParserTest {
    private PullRequestChainParser parser;

    @Before
    public void setUp() {
        parser = new PullRequestChainParser();
    }

    @Test
    public void parseChain_parseCorrectly() throws Exception {
        BitbucketPullRequest firstPr = createPr("a", "master");
        BitbucketPullRequest secondPr = createPr("b", "a");
        BitbucketPullRequests pullRequests = new BitbucketPullRequests();
        pullRequests.setPullRequests(asList(secondPr, firstPr));

        parser.parsePullRequestChains(pullRequests);

        assertEquals(1, firstPr.getBranchChain().size());
        assertEquals(2, secondPr.getBranchChain().size());
        assertTrue(firstPr.getBranchChain().contains("master"));
        assertTrue(secondPr.getBranchChain().containsAll(asList("a", "master")));
    }

    private BitbucketPullRequest createPr(String sourceBranch, String targetBranch) {
        BitbucketPullRequest pullRequest = new BitbucketPullRequest();
        pullRequest.setSource(createPrTarget(sourceBranch));
        pullRequest.setDestination(createPrTarget(targetBranch));

        return pullRequest;
    }

    private BitbucketPullRequestTarget createPrTarget(String branchName) {
        BitbucketPullRequestTarget target = new BitbucketPullRequestTarget();
        BitbucketBranch sourceBranch = new BitbucketBranch();
        sourceBranch.setName(branchName);
        target.setBranch(sourceBranch);

        return target;
    }
}
