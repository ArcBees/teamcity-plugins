package com.arcbees.bitbucket;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;
import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.util.BitbucketPullRequestBuild;
import com.arcbees.bitbucket.util.JsonCustomDataStorage;
import com.google.common.collect.Lists;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;

public class PullRequestCommentHandler {
    private final BitbucketApiFactory bitbucketApiFactory;
    private final Constants constants;
    private final WebLinks webLinks;

    public PullRequestCommentHandler(BitbucketApiFactory bitbucketApiFactory,
                                     Constants constants,
                                     WebLinks webLinks) {
        this.bitbucketApiFactory = bitbucketApiFactory;
        this.constants = constants;
        this.webLinks = webLinks;
    }

    public void process(SRunningBuild build, BuildTriggerDescriptor trigger) throws IOException {
        Branch branch = build.getBranch();
        if (branch != null) {
            SBuildType buildType = build.getBuildType();

            PropertiesHelper propertiesHelper = new PropertiesHelper(trigger.getProperties(), constants);
            BitbucketApi bitbucketApi = bitbucketApiFactory.create(
                    propertiesHelper.getUserName(),
                    propertiesHelper.getPassword(),
                    propertiesHelper.getRepositoryOwner(),
                    propertiesHelper.getRepositoryName());

            PullRequest pullRequest = bitbucketApi.getPullRequestForBranch(branch.getName());

            JsonCustomDataStorage<BitbucketPullRequestBuild> dataStorage = getJsonDataStorage(buildType, trigger);
            BitbucketPullRequestBuild pullRequestBuild =
                    getBitbucketPullRequestBuild(propertiesHelper, pullRequest, dataStorage);

            Comment comment = postOrUpdateComment(build, trigger, buildType, propertiesHelper, bitbucketApi,
                    pullRequest, pullRequestBuild);

            pullRequestBuild = new BitbucketPullRequestBuild(pullRequest, build.getBuildStatus(), comment);
            dataStorage.putValue(getPullRequestKey(propertiesHelper, pullRequest), pullRequestBuild);
        }
    }

    private Comment postOrUpdateComment(SRunningBuild build,
                                        BuildTriggerDescriptor trigger,
                                        SBuildType buildType,
                                        PropertiesHelper propertiesHelper,
                                        BitbucketApi bitbucketApi,
                                        PullRequest pullRequest,
                                        BitbucketPullRequestBuild pullRequestBuild) throws IOException {
        Comment comment = pullRequestBuild == null ? null : pullRequestBuild.getLastComment();
        if (shouldPostNewComment(build, trigger, propertiesHelper, pullRequest)) {
            deleteOldComment(bitbucketApi, propertiesHelper, pullRequest, buildType, trigger);
            Logger.getAnonymousLogger().severe("**** POSTING COMMENT");
            comment = bitbucketApi.postComment(pullRequest.getId(), getComment(build));
            Logger.getAnonymousLogger().severe("**** COMMENT POSTED");
        }

        return comment;
    }

    private void deleteOldComment(BitbucketApi bitbucketApi,
                                  PropertiesHelper propertiesHelper,
                                  PullRequest pullRequest,
                                  SBuildType buildType,
                                  BuildTriggerDescriptor trigger) throws IOException {
        BitbucketPullRequestBuild pullRequestBuild =
                getBitbucketPullRequestBuild(propertiesHelper, pullRequest, buildType, trigger);

        Comment lastComment = pullRequestBuild == null ? null : pullRequestBuild.getLastComment();
        if (lastComment != null) {
            Logger.getAnonymousLogger().severe("**** DELETING COMMENT");
            bitbucketApi.deleteComment(lastComment.getPullRequestId(), lastComment.getCommentId());
            Logger.getAnonymousLogger().severe("**** COMMENT DELETED");
        }
    }

    private boolean shouldPostNewComment(SRunningBuild build,
                                         BuildTriggerDescriptor trigger,
                                         PropertiesHelper propertiesHelper,
                                         PullRequest pullRequest) {
        BitbucketPullRequestBuild pullRequestBuild =
                getBitbucketPullRequestBuild(propertiesHelper, pullRequest, build.getBuildType(), trigger);

        boolean shouldPost = true;
        Logger.getAnonymousLogger().severe("PullRequestBuild : " + pullRequestBuild);
        if (pullRequestBuild != null) {
            Status status = build.getBuildStatus();
            Status lastStatus = pullRequestBuild.getLastStatus();

            Logger.getAnonymousLogger().severe("Status : " + status.isSuccessful());
            Logger.getAnonymousLogger().severe("LastStatus : " + lastStatus.isSuccessful());
            shouldPost = !(lastStatus.isSuccessful() && status.isSuccessful())
                    || pullRequestBuild.getLastComment() == null;
        }

        return shouldPost;
    }

    private BitbucketPullRequestBuild getBitbucketPullRequestBuild(PropertiesHelper propertiesHelper,
                                                                   PullRequest pullRequest,
                                                                   SBuildType buildType,
                                                                   BuildTriggerDescriptor trigger) {
        JsonCustomDataStorage<BitbucketPullRequestBuild> dataStorage = getJsonDataStorage(buildType, trigger);

        return getBitbucketPullRequestBuild(propertiesHelper, pullRequest, dataStorage);
    }

    private BitbucketPullRequestBuild getBitbucketPullRequestBuild(PropertiesHelper propertiesHelper,
                                                                   PullRequest pullRequest,
                                                                   JsonCustomDataStorage<BitbucketPullRequestBuild>
                                                                           dataStorage) {
        String pullRequestKey = getPullRequestKey(propertiesHelper.getRepositoryOwner(),
                propertiesHelper.getRepositoryName(), pullRequest);

        return dataStorage.getValue(pullRequestKey);
    }

    private JsonCustomDataStorage<BitbucketPullRequestBuild> getJsonDataStorage(SBuildType buildType,
                                                                                BuildTriggerDescriptor trigger) {
        String storageId = getStorageId(trigger);
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(storageId);

        return JsonCustomDataStorage.create(customDataStorage, BitbucketPullRequestBuild.class);
    }

    private String getPullRequestKey(PropertiesHelper helper, PullRequest pullRequest) {
        return getPullRequestKey(helper.getRepositoryOwner(), helper.getRepositoryName(), pullRequest);
    }

    private String getPullRequestKey(String repositoryOwner, String repositoryName, PullRequest pullRequest) {
        return constants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_" + pullRequest.getId();
    }

    private String getStorageId(BuildTriggerDescriptor triggerDescriptor) {
        return triggerDescriptor.getBuildTriggerService().getClass().getName() + "_"
                + getParametersSignature(triggerDescriptor);
    }

    private String getParametersSignature(BuildTriggerDescriptor triggerDescriptor) {
        Map<String, String> propsMap = triggerDescriptor.getParameters();
        List<String> keys = Lists.newArrayList(propsMap.keySet());
        Collections.sort(keys);

        StringBuilder signature = new StringBuilder();
        signature.append(triggerDescriptor.getType());
        for (String key : keys) {
            signature.append(key).append('=').append(propsMap.get(key));
        }

        return signature.toString();
    }

    private String getComment(SRunningBuild build) {
        return getComment(build.getBuildStatus()) + "(" + webLinks.getViewResultsUrl(build) + ")";
    }

    private String getComment(Status status) {
        if (status.isSuccessful()) {
            return "BUILD SUCCESS ";
        } else {
            return "BUILD FAILURE ";
        }
    }
}
