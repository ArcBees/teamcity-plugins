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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.apache.tomcat.maven.common.deployer.TomcatManager;
import org.apache.tomcat.maven.common.deployer.TomcatManagerException;
import org.apache.tomcat.maven.common.deployer.TomcatManagerResponse;
import org.jetbrains.annotations.NotNull;

import com.arcbees.bitbucket.BitbucketConstants;
import com.arcbees.bitbucket.BitbucketPropertiesHelper;
import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;
import com.arcbees.bitbucket.model.Comment;
import com.arcbees.bitbucket.model.PullRequest;
import com.arcbees.bitbucket.util.JsonCustomDataStorage;
import com.google.common.collect.Lists;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;

public class TomcatDeployHandler {
    private static final Logger LOGGER = Logger.getLogger(TomcatDeployHandler.class.getName());
    private static final String COMMENT_WEBAPP = "WebApp URL : ";

    private final BitbucketApiFactory bitbucketApiFactory;
    private final TomcatManagerFactory tomcatManagerFactory;
    private final BitbucketConstants bitbucketConstants;
    private final Constants constants;

    public TomcatDeployHandler(BitbucketApiFactory bitbucketApiFactory,
                               TomcatManagerFactory tomcatManagerFactory,
                               BitbucketConstants bitbucketConstants,
                               Constants constants) {
        this.bitbucketApiFactory = bitbucketApiFactory;
        this.tomcatManagerFactory = tomcatManagerFactory;
        this.bitbucketConstants = bitbucketConstants;
        this.constants = constants;
    }

    public void handle(SRunningBuild build, BuildTriggerDescriptor trigger) throws IOException {
        Branch branch = build.getBranch();
        if (branch != null && build.getBuildStatus().isSuccessful()) {
            SBuildType buildType = build.getBuildType();

            BitbucketPropertiesHelper bitbucketPropertiesHelper =
                    new BitbucketPropertiesHelper(trigger.getProperties(), bitbucketConstants);
            BitbucketApi bitbucketApi = bitbucketApiFactory.create(bitbucketPropertiesHelper);

            PullRequest pullRequest = bitbucketApi.getPullRequestForBranch(branch.getName());

            JsonCustomDataStorage<TomcatStagingDeploy> dataStorage = getJsonDataStorage(buildType, trigger);
            TomcatStagingDeploy stagingDeploy =
                    getTomcatStagingDeploy(bitbucketPropertiesHelper, pullRequest, dataStorage);

            if (stagingDeploy == null) {
                stagingDeploy = new TomcatStagingDeploy(pullRequest, false);
            }

            StagingPropertiesHelper stagingPropertiesHelper =
                    new StagingPropertiesHelper(trigger.getProperties(), constants);

            TomcatManager tomcatManager = createTomcatManager(stagingPropertiesHelper);

            boolean success = deploy(build, stagingPropertiesHelper.getBaseContext(), tomcatManager, stagingDeploy);
            stagingDeploy.setDeployed(success);

            LOGGER.severe(stagingDeploy.getWebPath() + " - " + success);
            if (success) {
                postComment(bitbucketApi, pullRequest, stagingDeploy);
            }

            dataStorage.putValue(getPullRequestKey(bitbucketPropertiesHelper, pullRequest), stagingDeploy);
        }
    }

    private boolean deploy(final SRunningBuild build,
                           final String baseContext,
                           final TomcatManager tomcatManager,
                           final TomcatStagingDeploy stagingDeploy) throws IOException {
        try {
            build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT)
                    .iterateArtifacts(new BuildArtifacts.BuildArtifactsProcessor() {
                        @NotNull
                        @Override
                        public Continuation processBuildArtifact(@NotNull BuildArtifact buildArtifact) {
                            if (buildArtifact.getName().endsWith(".war")) {
                                try {
                                    deployArtifact(buildArtifact, build, baseContext, tomcatManager, stagingDeploy);
                                } catch (TomcatManagerException | IOException e) {
                                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                                    throw new RuntimeException(e);
                                }
                            }
                            return Continuation.CONTINUE;
                        }
                    });

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void deployArtifact(BuildArtifact buildArtifact,
                                SRunningBuild build,
                                String baseContext,
                                TomcatManager tomcatManager,
                                TomcatStagingDeploy stagingDeploy)
            throws TomcatManagerException, IOException {
        assert build.getBranch() != null;

        File artifact = new File(build.getArtifactsDirectory(), buildArtifact.getRelativePath());
        String path = baseContext + "/" + build.getBranch().getName();

        String tomcatPath = UrlUtils.extractBaseUrl(tomcatManager.getURL());

        stagingDeploy.setWebPath(tomcatPath + path);

        TomcatManagerResponse response = tomcatManager.deploy(path, artifact, true, path, artifact.length());

        if (HttpStatus.SC_OK != response.getStatusCode()) {
            throw new TomcatManagerException(response.getReasonPhrase());
        }
    }

    private TomcatManager createTomcatManager(StagingPropertiesHelper propertiesHelper) {
        try {
            return tomcatManagerFactory.create(propertiesHelper);
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Comment postComment(BitbucketApi bitbucketApi,
                                PullRequest pullRequest,
                                TomcatStagingDeploy stagingDeploy) throws IOException {
        Comment comment = stagingDeploy.getComment();

        if (comment == null) {
            comment = bitbucketApi.postComment(pullRequest.getId(), getComment(stagingDeploy));
            stagingDeploy.setComment(comment);
        }

        return comment;
    }

    private TomcatStagingDeploy getTomcatStagingDeploy(BitbucketPropertiesHelper bitbucketPropertiesHelper,
                                                       PullRequest pullRequest,
                                                       JsonCustomDataStorage<TomcatStagingDeploy> dataStorage) {
        String pullRequestKey = getPullRequestKey(bitbucketPropertiesHelper.getRepositoryOwner(),
                bitbucketPropertiesHelper.getRepositoryName(), pullRequest);

        return dataStorage.getValue(pullRequestKey);
    }

    private JsonCustomDataStorage<TomcatStagingDeploy> getJsonDataStorage(SBuildType buildType,
                                                                          BuildTriggerDescriptor trigger) {
        String storageId = getStorageId(trigger);
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(storageId);

        return JsonCustomDataStorage.create(customDataStorage, TomcatStagingDeploy.class);
    }

    private String getPullRequestKey(BitbucketPropertiesHelper helper, PullRequest pullRequest) {
        return getPullRequestKey(helper.getRepositoryOwner(), helper.getRepositoryName(), pullRequest);
    }

    private String getPullRequestKey(String repositoryOwner, String repositoryName, PullRequest pullRequest) {
        return bitbucketConstants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_" + pullRequest
                .getId();
    }

    private String getStorageId(BuildTriggerDescriptor triggerDescriptor) {
        return triggerDescriptor.getBuildTriggerService().getClass().getName() + "_"
                + getParametersSignature(triggerDescriptor);
    }

    private String getParametersSignature(BuildTriggerDescriptor triggerDescriptor) {
        Map<String, String> propsMap = triggerDescriptor.getParameters();
        List<String> keys = Lists.newArrayList(propsMap.keySet());
        Collections.sort(keys);

        StringBuilder signature = new StringBuilder();
        signature.append(triggerDescriptor.getType());
        for (String key : keys) {
            signature.append(key).append('=').append(propsMap.get(key));
        }

        return signature.toString();
    }

    private String getComment(TomcatStagingDeploy stagingDeploy) {
        return COMMENT_WEBAPP + stagingDeploy.getWebPath();
    }
}
