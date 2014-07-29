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

package com.arcbees.staging;

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

public class StagingBuildListener {
    private static final Logger LOGGER = Logger.getLogger(StagingBuildListener.class.getName());

    private final TomcatDeployHandler deployHandler;
    private final ExecutorService executorService;

    public StagingBuildListener(EventDispatcher<BuildServerListener> listener,
                                ExecutorServices executorServices,
                                TomcatDeployHandler deployHandler) {
        this.deployHandler = deployHandler;
        executorService = executorServices.getLowPriorityExecutorService();

        listener.addListener(new BuildServerAdapter() {
            @Override
            public void buildFinished(@NotNull SRunningBuild build) {
                onBuildFinished(build);
            }
        });
    }

    private void onBuildFinished(final SRunningBuild build) {
        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return;
        }

        for (final BuildTriggerDescriptor trigger : buildType.getResolvedSettings().getBuildTriggersCollection()) {
            if (!trigger.getType().equals(TomcatStagingFeature.NAME)) {
                continue;
            }

            Branch branch = build.getBranch();
            if (branch != null) {
                handleDeploy(build, trigger);
            } else {
                LOGGER.severe("Unknown branch name");
            }
        }
    }

    private void handleDeploy(final SRunningBuild build, final BuildTriggerDescriptor trigger) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deployHandler.handle(build, trigger);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error getting pull request infos", e);
                }
            }
        });
    }
}
