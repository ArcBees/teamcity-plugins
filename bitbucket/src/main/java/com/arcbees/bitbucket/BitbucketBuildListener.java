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
                commentHandler.process(build, trigger);
            } else {
                LOGGER.severe("Unknown branch name");
            }
        }
    }
}
