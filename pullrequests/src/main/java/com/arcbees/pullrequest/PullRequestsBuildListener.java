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
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;

public class PullRequestsBuildListener {
    private static final Logger LOGGER = Logger.getLogger(PullRequestsBuildListener.class.getName());

    private final PullRequestStatusHandler statusHandler;
    private final ExecutorService executorService;

    public PullRequestsBuildListener(EventDispatcher<BuildServerListener> listener,
                                     ExecutorServices executorServices,
                                     PullRequestStatusHandler statusHandler) {
        this.statusHandler = statusHandler;
        executorService = executorServices.getLowPriorityExecutorService();

        listener.addListener(new BuildServerAdapter() {
            @Override
            public void buildStarted(@NotNull SRunningBuild build) {
                onBuildStatusChanged(build, BuildStatus.STARTING);
            }

            @Override
            public void buildInterrupted(@NotNull SRunningBuild build) {
                onBuildStatusChanged(build, BuildStatus.INTERRUPTED);
            }

            @Override
            public void buildFinished(@NotNull SRunningBuild build) {
                onBuildStatusChanged(build, BuildStatus.FINISHED);
            }
        });
    }

    private void onBuildStatusChanged(final SRunningBuild build,
                                      final BuildStatus buildStatus) {
        final BuildTriggerDescriptor trigger = getTrigger(build);

        if (trigger != null) {
            Branch branch = build.getBranch();
            if (branch != null) {
                handleBuildStatus(build, buildStatus, trigger);
            } else {
                LOGGER.severe("Unknown branch name");
            }
        }
    }

    private void handleBuildStatus(final SRunningBuild build,
                                   final BuildStatus buildStatus,
                                   final BuildTriggerDescriptor trigger) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    statusHandler.handle(build, trigger, buildStatus);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error updating pull request status.", e);
                }
            }
        });
    }

    private BuildTriggerDescriptor getTrigger(SRunningBuild build) {
        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return null;
        }

        for (BuildTriggerDescriptor trigger : buildType.getResolvedSettings().getBuildTriggersCollection()) {
            if (trigger.getType().equals(PullRequestsFeature.NAME)) {
                return trigger;
            }
        }

        return null;
    }
}
