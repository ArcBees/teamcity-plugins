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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerService;
import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class BitbucketPullRequestFeature extends BuildTriggerService {
    public static final String NAME = "BitbucketPullRequest";

    private static final String DISPLAY_NAME = "Bitbucket Pull Requests";
    private static final String EDIT_URL = "bitbucket_pr.jsp";

    private final BitbucketPropertiesProcessor propertiesProcessor;
    private final BitbucketPullRequestTrigger triggeringPolicy;
    private final PluginDescriptor pluginDescriptor;

    public BitbucketPullRequestFeature(BitbucketPullRequestTrigger triggeringPolicy,
                                       BitbucketPropertiesProcessor propertiesProcessor,
                                       PluginDescriptor pluginDescriptor) {
        this.triggeringPolicy = triggeringPolicy;
        this.propertiesProcessor = propertiesProcessor;
        this.pluginDescriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @NotNull
    @Override
    public String describeTrigger(@NotNull BuildTriggerDescriptor buildTriggerDescriptor) {
        return DISPLAY_NAME;
    }

    @NotNull
    @Override
    public BuildTriggeringPolicy getBuildTriggeringPolicy() {
        return triggeringPolicy;
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return pluginDescriptor.getPluginResourcesPath(EDIT_URL);
    }

    @Nullable
    @Override
    public PropertiesProcessor getTriggerPropertiesProcessor() {
        return propertiesProcessor;
    }
}
