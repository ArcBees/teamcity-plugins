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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.util.EventDispatcher;

public class PullRequestsBuildListener {
    private static final Logger LOGGER = Logger.getLogger(PullRequestsBuildListener.class.getName());

    private final PullRequestStatusHandler statusHandler;

    public PullRequestsBuildListener(EventDispatcher<BuildServerListener> listener,
                                     PullRequestStatusHandler statusHandler) {
        this.statusHandler = statusHandler;
        listener.addListener(new BuildServerAdapter() {
            @Override
            public void buildStarted(@NotNull SRunningBuild build) {
                try {
                    onBuildStatusChanged(build, BuildStatus.STARTING);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error getting pull request infos", e);
                }
            }

            @Override
            public void buildInterrupted(@NotNull SRunningBuild build) {
                try {
                    onBuildStatusChanged(build, BuildStatus.INTERRUPTED);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error getting pull request infos", e);
                }
            }

            @Override
            public void buildFinished(@NotNull SRunningBuild build) {
                try {
                    onBuildStatusChanged(build, BuildStatus.FINISHED);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error getting pull request infos", e);
                }
            }
        });
    }

    private void onBuildStatusChanged(SRunningBuild build, BuildStatus buildStatus) throws IOException {
        BuildTriggerDescriptor trigger = getTrigger(build);

        if (trigger != null) {
            Branch branch = build.getBranch();
            if (branch != null) {
                statusHandler.handle(build, trigger, buildStatus);
            } else {
                LOGGER.severe("Unknown branch name");
            }
        }
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
