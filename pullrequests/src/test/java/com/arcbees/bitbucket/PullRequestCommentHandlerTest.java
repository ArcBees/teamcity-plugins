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

package com.arcbees.bitbucket;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;
import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.PullRequest;
import com.google.gson.Gson;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerService;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PullRequestCommentHandlerTest {
    private PullRequestCommentHandler commentHandler;
    private BitbucketApi bitbucketApi;
    private BuildTriggerDescriptor trigger;
    private CustomDataStorage dataStorage;
    private SRunningBuild build;

    @Before
    public void setUp() throws IOException {
        BitbucketApiFactory apiFactory = mock(BitbucketApiFactory.class);
        bitbucketApi = mock(BitbucketApi.class);
        given(apiFactory.create(any(BitbucketPropertiesHelper.class))).willReturn(bitbucketApi);

        commentHandler = new PullRequestCommentHandler(apiFactory, new BitbucketConstants(), mock(WebLinks.class));

        trigger = mock(BuildTriggerDescriptor.class);

        SBuildType buildType = mock(SBuildType.class);
        dataStorage = mock(CustomDataStorage.class);
        given(buildType.getCustomDataStorage(anyString())).willReturn(dataStorage);

        build = mock(SRunningBuild.class);
        given(build.getBuildType()).willReturn(buildType);
        given(build.getBranch()).willReturn(mock(Branch.class));

        given(trigger.getBuildTriggerService()).willReturn(mock(BuildTriggerService.class));
        given(bitbucketApi.getPullRequestForBranch(anyString())).willReturn(createPullRequest());
    }

    @Test
    public void firstBuild_postComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.NORMAL);

        commentHandler.handle(build, trigger);

        verify(bitbucketApi, never()).deleteComment(anyInt(), anyLong());
        verify(bitbucketApi).postComment(anyInt(), anyString());
    }

    @Test
    public void secondSuccess_newComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.NORMAL);
        BitbucketPullRequestBuild pullRequestBuild =
                new BitbucketPullRequestBuild(createPullRequest(), Status.NORMAL, new Comment());

        given(dataStorage.getValue(anyString())).willReturn(new Gson().toJson(pullRequestBuild));

        commentHandler.handle(build, trigger);

        verify(bitbucketApi, times(1)).deleteComment(anyInt(), anyLong());
        verify(bitbucketApi, times(1)).postComment(anyInt(), anyString());
    }

    @Test
    public void failureAfterSuccess_newComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.FAILURE);
        BitbucketPullRequestBuild pullRequestBuild =
                new BitbucketPullRequestBuild(createPullRequest(), Status.NORMAL, new Comment());

        given(dataStorage.getValue(anyString())).willReturn(new Gson().toJson(pullRequestBuild));

        commentHandler.handle(build, trigger);

        verify(bitbucketApi, times(1)).deleteComment(anyInt(), anyLong());
        verify(bitbucketApi, times(1)).postComment(anyInt(), anyString());
    }

    @Test
    public void failureAfterFailure_newComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.FAILURE);
        BitbucketPullRequestBuild pullRequestBuild =
                new BitbucketPullRequestBuild(createPullRequest(), Status.FAILURE, new Comment());

        given(dataStorage.getValue(anyString())).willReturn(new Gson().toJson(pullRequestBuild));

        commentHandler.handle(build, trigger);

        verify(bitbucketApi, times(1)).deleteComment(anyInt(), anyLong());
        verify(bitbucketApi, times(1)).postComment(anyInt(), anyString());
    }

    private PullRequest createPullRequest() {
        PullRequest pullRequest = new PullRequest();
        pullRequest.setId(1);

        return pullRequest;
    }
}
