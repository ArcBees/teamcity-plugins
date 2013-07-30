package com.arcbees.bitbucket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;
import com.arcbees.bitbucket.model.PullRequest;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.util.EventDispatcher;

import static jetbrains.buildServer.messages.Status.NORMAL;

public class BitbucketBuildListener {
    private static final Logger LOGGER = Logger.getLogger(BitbucketBuildListener.class.getName());

    private final BitbucketApiFactory bitbucketApiFactory;
    private final WebLinks webLinks;
    private final Constants constants;

    public BitbucketBuildListener(EventDispatcher<BuildServerListener> listener,
                                  BitbucketApiFactory bitbucketApiFactory,
                                  WebLinks webLinks,
                                  Constants constants) {
        this.bitbucketApiFactory = bitbucketApiFactory;
        this.webLinks = webLinks;
        this.constants = constants;

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
        if (buildType == null) return;

        for (BuildTriggerDescriptor trigger : buildType.getResolvedSettings().getBuildTriggersCollection()) {
            if (!trigger.getType().equals(BitbucketPullRequestFeature.NAME)) continue;

            PropertiesHelper propertiesHelper = new PropertiesHelper(trigger.getProperties(), constants);
            Branch branch = build.getBranch();
            if (branch != null) {
                BitbucketApi bitbucketApi = bitbucketApiFactory.create(propertiesHelper.getUserName(),
                        propertiesHelper.getPassword());
                String repositoryOwner = propertiesHelper.getRepositoryOwner();
                String repositoryName = propertiesHelper.getRepositoryName();

                PullRequest pullRequest = bitbucketApi.getPullRequestForBranch(repositoryOwner,
                        repositoryName, branch.getName());

                bitbucketApi.postComment(repositoryOwner, repositoryName, pullRequest.getId(),
                        getComment(build));
            } else {
                LOGGER.severe("Unknown branch name");
            }
        }
    }

    private String getComment(SRunningBuild build) {
        return getComment(build.getBuildStatus()) + "(" + webLinks.getViewResultsUrl(build) + ")";
    }

    private String getComment(Status status) {
        if (NORMAL.equals(status)) {
            return "BUILD SUCCESS ";
        } else {
            return "BUILD FAILURE ";
        }
    }
}
