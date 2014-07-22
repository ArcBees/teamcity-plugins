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

package com.arcbees.vcs.bitbucket;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.arcbees.pullrequest.Constants;
import com.arcbees.pullrequest.PullRequestBuild;
import com.arcbees.pullrequest.PullRequestCommentHandler;
import com.arcbees.vcs.VcsApi;
import com.arcbees.vcs.VcsApiFactories;
import com.arcbees.vcs.VcsConstants;
import com.arcbees.vcs.VcsPropertiesHelper;
import com.arcbees.vcs.bitbucket.model.BitbucketComment;
import com.arcbees.vcs.bitbucket.model.BitbucketPullRequest;
import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.util.PolymorphicTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    private VcsApi vcsApi;
    private BuildTriggerDescriptor trigger;
    private CustomDataStorage dataStorage;
    private SRunningBuild build;

    @Before
    public void setUp() throws IOException {
        VcsApiFactories apiFactory = mock(VcsApiFactories.class);
        vcsApi = mock(VcsApi.class);
        given(apiFactory.create(any(VcsPropertiesHelper.class))).willReturn(vcsApi);

        commentHandler = new PullRequestCommentHandler(apiFactory, new VcsConstants(), new Constants(),
                mock(WebLinks.class));

        trigger = mock(BuildTriggerDescriptor.class);

        SBuildType buildType = mock(SBuildType.class);
        dataStorage = mock(CustomDataStorage.class);
        given(buildType.getCustomDataStorage(anyString())).willReturn(dataStorage);

        build = mock(SRunningBuild.class);
        given(build.getBuildType()).willReturn(buildType);
        given(build.getBranch()).willReturn(mock(Branch.class));

        given(trigger.getBuildTriggerService()).willReturn(mock(BuildTriggerService.class));
        given(vcsApi.getPullRequestForBranch(anyString())).willReturn(createPullRequest());
    }

    @Test
    public void firstBuild_postComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.NORMAL);

        commentHandler.handle(build, trigger);

        verify(vcsApi, never()).deleteComment(anyInt(), anyLong());
        verify(vcsApi).postComment(anyInt(), anyString());
    }

    @Test
    public void secondSuccess_newComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.NORMAL);
        PullRequestBuild pullRequestBuild =
                new PullRequestBuild(createPullRequest(), Status.NORMAL, new BitbucketComment());

        given(dataStorage.getValue(anyString())).willReturn(getGson().toJson(pullRequestBuild));

        commentHandler.handle(build, trigger);

        verify(vcsApi, times(1)).deleteComment(anyInt(), anyLong());
        verify(vcsApi, times(1)).postComment(anyInt(), anyString());
    }

    @Test
    public void failureAfterSuccess_newComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.FAILURE);
        PullRequestBuild pullRequestBuild =
                new PullRequestBuild(createPullRequest(), Status.NORMAL, new BitbucketComment());

        given(dataStorage.getValue(anyString())).willReturn(getGson().toJson(pullRequestBuild));

        commentHandler.handle(build, trigger);

        verify(vcsApi, times(1)).deleteComment(anyInt(), anyLong());
        verify(vcsApi, times(1)).postComment(anyInt(), anyString());
    }

    @Test
    public void failureAfterFailure_newComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.FAILURE);
        PullRequestBuild pullRequestBuild =
                new PullRequestBuild(createPullRequest(), Status.FAILURE, new BitbucketComment());

        given(dataStorage.getValue(anyString())).willReturn(getGson().toJson(pullRequestBuild));

        commentHandler.handle(build, trigger);

        verify(vcsApi, times(1)).deleteComment(anyInt(), anyLong());
        verify(vcsApi, times(1)).postComment(anyInt(), anyString());
    }

    private PullRequest createPullRequest() {
        PullRequest pullRequest = new BitbucketPullRequest();
        pullRequest.setId(1);

        return pullRequest;
    }

    private Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(PullRequest.class, new PolymorphicTypeAdapter<PullRequest>())
                .registerTypeAdapter(Comment.class, new PolymorphicTypeAdapter<Comment>())
                .create();
    }
}
