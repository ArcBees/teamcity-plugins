/**
 * Copyright 2016 ArcBees Inc.
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

package com.arcbees.feature;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.arcbees.vcs.VcsPropertiesProcessor;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class BuildCommitFeature extends BuildFeature {
    public static final String NAME = "teamcity.build.commit.status";

    private static final String DISPLAY_NAME = "Report commit build status";
    private static final String EDIT_URL = "feature.jsp";

    private final VcsPropertiesProcessor propertiesProcessor;
    private final PluginDescriptor pluginDescriptor;

    public BuildCommitFeature(
            VcsPropertiesProcessor propertiesProcessor,
            PluginDescriptor pluginDescriptor) {
        this.propertiesProcessor = propertiesProcessor;
        this.pluginDescriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public String getType() {
        return NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return pluginDescriptor.getPluginResourcesPath(EDIT_URL);
    }

    @Nullable
    @Override
    public PropertiesProcessor getParametersProcessor() {
        return propertiesProcessor;
    }
}
