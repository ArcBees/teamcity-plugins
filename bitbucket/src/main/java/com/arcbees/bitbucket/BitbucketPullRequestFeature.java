package com.arcbees.bitbucket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerService;
import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class BitbucketPullRequestFeature extends BuildTriggerService {
    public static final String NAME = "Bitbucket Pull Request";

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
        return buildTriggerDescriptor.getTriggerName();
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
