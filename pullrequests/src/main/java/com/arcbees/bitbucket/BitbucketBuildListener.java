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
import java.util.logging.Level;
import java.util.logging.Logger;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.util.EventDispatcher;

public class BitbucketBuildListener {
    private static final Logger LOGGER = Logger.getLogger(BitbucketBuildListener.class.getName());

    private final PullRequestCommentHandler commentHandler;

    public BitbucketBuildListener(EventDispatcher<BuildServerListener> listener,
                                  PullRequestCommentHandler commentHandler) {
        this.commentHandler = commentHandler;
        listener.addListener(new BuildServerAdapter() {
            @Override
            public void buildFinished(SRunningBuild build) {
                try {
                    onBuildFinished(build);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error getting pull request infos", e);
                }
            }
        });
    }

    private void onBuildFinished(SRunningBuild build) throws IOException {
        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return;
        }

        for (BuildTriggerDescriptor trigger : buildType.getResolvedSettings().getBuildTriggersCollection()) {
            if (!trigger.getType().equals(BitbucketPullRequestFeature.NAME)) {
                continue;
            }

            Branch branch = build.getBranch();
            if (branch != null) {
                commentHandler.handle(build, trigger);
            } else {
                LOGGER.severe("Unknown branch name");
            }
        }
    }
}
