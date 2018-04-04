/*
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

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.arcbees.vcs.VcsApi;
import com.arcbees.vcs.VcsApiFactories;
import com.arcbees.vcs.VcsConstants;
import com.arcbees.vcs.VcsPropertiesHelper;
import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.Commit;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;
import com.arcbees.vcs.model.PullRequests;
import com.arcbees.vcs.util.JsonCustomDataStorage;
import com.google.common.base.Strings;
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

public class PullRequestsTrigger extends PolledBuildTrigger {
    private final VcsApiFactories vcsApiFactories;
    private final BatchTrigger batchTrigger;
    private final VcsConstants vcsConstants;
    private final BuildCustomizerFactory buildCustomizerFactory;
    private final PullRequestChainParser pullRequestChainParser;

    public PullRequestsTrigger(
            VcsApiFactories vcsApiFactories,
            BatchTrigger batchTrigger,
            VcsConstants vcsConstants,
            BuildCustomizerFactory buildCustomizerFactory,
            PullRequestChainParser pullRequestChainParser) {
        this.vcsApiFactories = vcsApiFactories;
        this.batchTrigger = batchTrigger;
        this.vcsConstants = vcsConstants;
        this.buildCustomizerFactory = buildCustomizerFactory;
        this.pullRequestChainParser = pullRequestChainParser;
    }

    @Override
    public void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
        BuildTriggerDescriptor triggerDescriptor = context.getTriggerDescriptor();
        Map<String, String> properties = triggerDescriptor.getProperties();
        VcsPropertiesHelper vcsPropertiesHelper = new VcsPropertiesHelper(properties, vcsConstants);
        String repositoryOwner = vcsPropertiesHelper.getRepositoryOwner();
        String repositoryName = vcsPropertiesHelper.getRepositoryName();

        VcsApi vcsApi = vcsApiFactories.create(vcsPropertiesHelper);
        try {
            PullRequests<? extends PullRequest> pullRequests = vcsApi.getOpenedPullRequests();
            pullRequestChainParser.parsePullRequestChains(pullRequests);

            JsonCustomDataStorage<PullRequestBuild> dataStorage =
                    JsonCustomDataStorage.create(context.getCustomDataStorage(), PullRequestBuild.class);

            List<TriggerTask> triggerTasks = Lists.newArrayList();
            for (PullRequest pullRequest : pullRequests.getPullRequests()) {
                if (shouldBuildPullRequest(vcsPropertiesHelper, pullRequest)) {
                    String pullRequestKey = getPullRequestKey(repositoryOwner, repositoryName, pullRequest);
                    PullRequestBuild pullRequestBuild = dataStorage.getValue(pullRequestKey);

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
                        pullRequestBuild = new PullRequestBuild(pullRequest, lastStatus, lastComment);
                        dataStorage.putValue(pullRequestKey, pullRequestBuild);
                    }
                }
            }

            batchTrigger.processTasks(triggerTasks, triggerDescriptor.getTriggerName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldBuildPullRequest(VcsPropertiesHelper vcsPropertiesHelper, PullRequest pullRequest) {
        String baseBranch = vcsPropertiesHelper.getBaseBranch();

        return Strings.isNullOrEmpty(baseBranch) || pullRequest.getBranchChain().contains(baseBranch);
    }

    private boolean addBuildTask(PolledTriggerContext context, List<TriggerTask> triggerTasks, PullRequest pullRequest,
            String lastTriggeredCommitHash) {
        PullRequestTarget source = pullRequest.getSource();
        Commit lastCommit = source.getCommit();

        boolean added = false;
        if (!lastCommit.getHash().equals(lastTriggeredCommitHash)) {
            addBuildTask(context, triggerTasks, pullRequest);
            added = true;
        }

        return added;
    }

    private void addBuildTask(PolledTriggerContext context, List<TriggerTask> triggerTasks, PullRequest pullRequest) {
        PullRequestTarget source = pullRequest.getSource();
        BuildTypeEx buildType = (BuildTypeEx) context.getBuildType();

        BuildCustomizer buildCustomizer = buildCustomizerFactory.createBuildCustomizer(buildType, null);
        buildCustomizer.setCleanSources(true);

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("trigger.pullRequestId", String.valueOf(pullRequest.getId()));
        buildCustomizer.setParameters(parameters);

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
        return vcsConstants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_" + pullRequest.getId();
    }
}
