/*
 * Copyright 2013 ArcBees Inc.
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
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;
import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.Commit;
import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.model.PullRequestTarget;
import com.arcbees.bitbucket.model.PullRequests;
import com.arcbees.bitbucket.util.BitbucketPullRequestBuild;
import com.arcbees.bitbucket.util.JsonCustomDataStorage;
import com.google.common.collect.Lists;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BatchTrigger;
import jetbrains.buildServer.serverSide.BranchEx;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.BuildTypeEx;
import jetbrains.buildServer.serverSide.TriggerTask;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;

public class BitbucketPullRequestTrigger extends PolledBuildTrigger {
    private final BitbucketApiFactory apiFactory;
    private final BatchTrigger batchTrigger;
    private final Constants constants;
    private final BuildCustomizerFactory buildCustomizerFactory;

    public BitbucketPullRequestTrigger(BitbucketApiFactory apiFactory,
                                       BatchTrigger batchTrigger,
                                       Constants constants,
                                       BuildCustomizerFactory buildCustomizerFactory) {
        this.apiFactory = apiFactory;
        this.batchTrigger = batchTrigger;
        this.constants = constants;
        this.buildCustomizerFactory = buildCustomizerFactory;
    }

    @Override
    public void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
        BuildTriggerDescriptor triggerDescriptor = context.getTriggerDescriptor();
        Map<String, String> properties = triggerDescriptor.getProperties();

        PropertiesHelper propertiesHelper = new PropertiesHelper(properties, constants);
        String repositoryOwner = propertiesHelper.getRepositoryOwner();
        String repositoryName = propertiesHelper.getRepositoryName();

        BitbucketApi bitbucketApi = apiFactory.create(propertiesHelper);
        try {
            PullRequests pullRequests = bitbucketApi.getOpenedPullRequests();
            JsonCustomDataStorage<BitbucketPullRequestBuild> dataStorage =
                    JsonCustomDataStorage.create(context.getCustomDataStorage(), BitbucketPullRequestBuild.class);

            List<TriggerTask> triggerTasks = Lists.newArrayList();
            for (PullRequest pullRequest : pullRequests.getPullRequests()) {
                String pullRequestKey = getPullRequestKey(repositoryOwner, repositoryName, pullRequest);
                BitbucketPullRequestBuild pullRequestBuild = dataStorage.getValue(pullRequestKey);

                String lastTriggeredCommitHash = "";
                Status lastStatus = Status.UNKNOWN;
                Comment lastComment = null;

                if (pullRequestBuild != null) {
                    lastTriggeredCommitHash = pullRequestBuild.getLastCommitHash();
                    lastComment = pullRequestBuild.getLastComment();
                    lastStatus = pullRequestBuild.getLastStatus();
                }

                boolean buildAdded = addBuildTask(context, triggerTasks, pullRequest, lastTriggeredCommitHash);
                if (buildAdded) {
                    pullRequestBuild = new BitbucketPullRequestBuild(pullRequest, lastStatus, lastComment);
                    dataStorage.putValue(pullRequestKey, pullRequestBuild);
                }
            }

            batchTrigger.processTasks(triggerTasks, triggerDescriptor.getTriggerName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean addBuildTask(PolledTriggerContext context, List<TriggerTask> triggerTasks, PullRequest pullRequest,
                                 String lastTriggeredCommitHash) {
        PullRequestTarget source = pullRequest.getSource();
        Commit lastCommit = source.getCommit();

        boolean added = false;
        if (!lastCommit.getHash().equals(lastTriggeredCommitHash)) {
            addBuildTask(context, triggerTasks, source);
            added = true;
        }

        return added;
    }

    private void addBuildTask(PolledTriggerContext context, List<TriggerTask> triggerTasks, PullRequestTarget source) {
        BuildTypeEx buildType = (BuildTypeEx) context.getBuildType();
        BuildCustomizer buildCustomizer = buildCustomizerFactory.createBuildCustomizer(buildType, null);
        buildCustomizer.setCleanSources(true);

        BranchEx branch = buildType.getBranchByDisplayName(source.getBranch().getName());
        SVcsModification lastModification = checkChanges(source.getCommit().getHash(),
                branch.getDummyBuild().getChanges(SelectPrevBuildPolicy.SINCE_NULL_BUILD, true));

        buildCustomizer.setDesiredBranchName(branch.getName());

        if (lastModification != null) {
            buildCustomizer.setChangesUpTo(lastModification);
        }

        TriggerTask task = batchTrigger.newTriggerTask(buildCustomizer.createPromotion());
        triggerTasks.add(task);
    }

    private SVcsModification checkChanges(String commitHash, List<SVcsModification> changes) {
        for (SVcsModification sVcsModification : changes) {
            if (sVcsModification.getVersion().startsWith(commitHash)) {
                return sVcsModification;
            }
        }

        return null;
    }

    private String getPullRequestKey(String repositoryOwner, String repositoryName, PullRequest pullRequest) {
        return constants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_" + pullRequest.getId();
    }
}
