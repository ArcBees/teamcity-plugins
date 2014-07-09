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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.arcbees.vcs.VcsApi;
import com.arcbees.vcs.VcsApiFactories;
import com.arcbees.vcs.VcsConstants;
import com.arcbees.vcs.VcsPropertiesHelper;
import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.util.JsonCustomDataStorage;
import com.google.common.collect.Lists;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;

public class PullRequestCommentHandler {
    private final VcsApiFactories vcsApiFactories;
    private final VcsConstants vcsConstants;
    private final Constants constants;
    private final WebLinks webLinks;

    public PullRequestCommentHandler(VcsApiFactories vcsApiFactories,
                                     VcsConstants vcsConstants,
                                     Constants constants,
                                     WebLinks webLinks) {
        this.vcsApiFactories = vcsApiFactories;
        this.vcsConstants = vcsConstants;
        this.constants = constants;
        this.webLinks = webLinks;
    }

    public void handle(SRunningBuild build, BuildTriggerDescriptor trigger) throws IOException {
        Branch branch = build.getBranch();
        if (branch != null) {
            SBuildType buildType = build.getBuildType();

            VcsPropertiesHelper vcsPropertiesHelper = new VcsPropertiesHelper(trigger.getProperties(), vcsConstants);
            VcsApi vcsApi = vcsApiFactories.create(vcsPropertiesHelper);

            PullRequest pullRequest = vcsApi.getPullRequestForBranch(branch.getName());

            JsonCustomDataStorage<PullRequestBuild> dataStorage = getJsonDataStorage(buildType, trigger);
            PullRequestBuild pullRequestBuild =
                    getPullRequestBuild(vcsPropertiesHelper, pullRequest, dataStorage);

            Comment comment = postOrUpdateComment(build, vcsApi, pullRequest, pullRequestBuild);

            pullRequestBuild = new PullRequestBuild(pullRequest, build.getBuildStatus(), comment);
            dataStorage.putValue(getPullRequestKey(vcsPropertiesHelper, pullRequest), pullRequestBuild);
        }
    }

    private Comment postOrUpdateComment(SRunningBuild build,
                                        VcsApi bitbucketApi,
                                        PullRequest pullRequest,
                                        PullRequestBuild pullRequestBuild) throws IOException {
        Comment comment = pullRequestBuild == null ? null : pullRequestBuild.getLastComment();

        if (comment != null) {
            deleteOldComment(bitbucketApi, pullRequest.getId(), comment);
        }

        comment = bitbucketApi.postComment(pullRequest.getId(), getComment(build));

        return comment;
    }

    private void deleteOldComment(VcsApi vcsApi,
                                  int pullRequestId,
                                  Comment oldComment) throws IOException {
        vcsApi.deleteComment(pullRequestId, oldComment.getCommentId());
    }

    private PullRequestBuild getPullRequestBuild(VcsPropertiesHelper vcsPropertiesHelper,
                                                 PullRequest pullRequest,
                                                 JsonCustomDataStorage<PullRequestBuild> dataStorage) {
        String pullRequestKey = getPullRequestKey(vcsPropertiesHelper.getRepositoryOwner(),
                vcsPropertiesHelper.getRepositoryName(), pullRequest);

        return dataStorage.getValue(pullRequestKey);
    }

    private JsonCustomDataStorage<PullRequestBuild> getJsonDataStorage(SBuildType buildType,
                                                                       BuildTriggerDescriptor trigger) {
        String storageId = getStorageId(trigger);
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(storageId);

        return JsonCustomDataStorage.create(customDataStorage, PullRequestBuild.class);
    }

    private String getPullRequestKey(VcsPropertiesHelper helper, PullRequest pullRequest) {
        return getPullRequestKey(helper.getRepositoryOwner(), helper.getRepositoryName(), pullRequest);
    }

    private String getPullRequestKey(String repositoryOwner, String repositoryName, PullRequest pullRequest) {
        return vcsConstants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_" + pullRequest.getId();
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
            return constants.getBuildSuccess();
        } else {
            return constants.getBuildFailure();
        }
    }
}
