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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;
import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.PullRequest;
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
    private final BitbucketConstants bitbucketConstants;
    private final WebLinks webLinks;

    public PullRequestCommentHandler(BitbucketApiFactory bitbucketApiFactory,
                                     BitbucketConstants bitbucketConstants,
                                     WebLinks webLinks) {
        this.bitbucketApiFactory = bitbucketApiFactory;
        this.bitbucketConstants = bitbucketConstants;
        this.webLinks = webLinks;
    }

    public void handle(SRunningBuild build, BuildTriggerDescriptor trigger) throws IOException {
        Branch branch = build.getBranch();
        if (branch != null) {
            SBuildType buildType = build.getBuildType();

            BitbucketPropertiesHelper bitbucketPropertiesHelper = new BitbucketPropertiesHelper(trigger.getProperties(),
                    bitbucketConstants);
            BitbucketApi bitbucketApi = bitbucketApiFactory.create(bitbucketPropertiesHelper);

            PullRequest pullRequest = bitbucketApi.getPullRequestForBranch(branch.getName());

            JsonCustomDataStorage<BitbucketPullRequestBuild> dataStorage = getJsonDataStorage(buildType, trigger);
            BitbucketPullRequestBuild pullRequestBuild =
                    getBitbucketPullRequestBuild(bitbucketPropertiesHelper, pullRequest, dataStorage);

            Comment comment = postOrUpdateComment(build, bitbucketApi, pullRequest, pullRequestBuild);

            pullRequestBuild = new BitbucketPullRequestBuild(pullRequest, build.getBuildStatus(), comment);
            dataStorage.putValue(getPullRequestKey(bitbucketPropertiesHelper, pullRequest), pullRequestBuild);
        }
    }

    private Comment postOrUpdateComment(SRunningBuild build,
                                        BitbucketApi bitbucketApi,
                                        PullRequest pullRequest,
                                        BitbucketPullRequestBuild pullRequestBuild) throws IOException {
        Comment comment = pullRequestBuild == null ? null : pullRequestBuild.getLastComment();

        if (comment != null) {
            deleteOldComment(bitbucketApi, comment);
        }

        comment = bitbucketApi.postComment(pullRequest.getId(), getComment(build));

        return comment;
    }

    private void deleteOldComment(BitbucketApi bitbucketApi,
                                  Comment oldComment) throws IOException {
        bitbucketApi.deleteComment(oldComment.getPullRequestId(), oldComment.getCommentId());
    }

    private BitbucketPullRequestBuild getBitbucketPullRequestBuild(BitbucketPropertiesHelper bitbucketPropertiesHelper,
                                                                   PullRequest pullRequest,
                                                                   JsonCustomDataStorage<BitbucketPullRequestBuild>
                                                                           dataStorage) {
        String pullRequestKey = getPullRequestKey(bitbucketPropertiesHelper.getRepositoryOwner(),
                bitbucketPropertiesHelper.getRepositoryName(), pullRequest);

        return dataStorage.getValue(pullRequestKey);
    }

    private JsonCustomDataStorage<BitbucketPullRequestBuild> getJsonDataStorage(SBuildType buildType,
                                                                                BuildTriggerDescriptor trigger) {
        String storageId = getStorageId(trigger);
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(storageId);

        return JsonCustomDataStorage.create(customDataStorage, BitbucketPullRequestBuild.class);
    }

    private String getPullRequestKey(BitbucketPropertiesHelper helper, PullRequest pullRequest) {
        return getPullRequestKey(helper.getRepositoryOwner(), helper.getRepositoryName(), pullRequest);
    }

    private String getPullRequestKey(String repositoryOwner, String repositoryName, PullRequest pullRequest) {
        return bitbucketConstants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_" + pullRequest
                .getId();
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
            return bitbucketConstants.getBuildSuccess();
        } else {
            return bitbucketConstants.getBuildFailure();
        }
    }
}
