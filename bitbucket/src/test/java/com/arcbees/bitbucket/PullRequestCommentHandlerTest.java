package com.arcbees.bitbucket;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;
import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.util.BitbucketPullRequestBuild;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PullRequestCommentHandlerTest {
    private PullRequestCommentHandler commentHandler;
    private BitbucketApi bitbucketApi;
    private BuildTriggerDescriptor trigger;
    private SBuildType buildType;
    private CustomDataStorage dataStorage;
    private SRunningBuild build;

    @Before
    public void setUp() throws IOException {
        BitbucketApiFactory apiFactory = mock(BitbucketApiFactory.class);
        bitbucketApi = mock(BitbucketApi.class);
        given(apiFactory.create(any(PropertiesHelper.class))).willReturn(bitbucketApi);

        commentHandler = new PullRequestCommentHandler(apiFactory, new Constants(), mock(WebLinks.class));

        trigger = mock(BuildTriggerDescriptor.class);

        buildType = mock(SBuildType.class);
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

        verify(bitbucketApi, times(0)).deleteComment(anyInt(), anyLong());
        verify(bitbucketApi).postComment(anyInt(), anyString());
    }

    @Test
    public void secondSuccess_noComment() throws IOException {
        given(build.getBuildStatus()).willReturn(Status.NORMAL);
        BitbucketPullRequestBuild pullRequestBuild =
                new BitbucketPullRequestBuild(createPullRequest(), Status.NORMAL, new Comment());

        given(dataStorage.getValue(anyString())).willReturn(new Gson().toJson(pullRequestBuild));

        commentHandler.handle(build, trigger);

        verify(bitbucketApi, times(0)).deleteComment(anyInt(), anyLong());
        verify(bitbucketApi, times(0)).postComment(anyInt(), anyString());
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
