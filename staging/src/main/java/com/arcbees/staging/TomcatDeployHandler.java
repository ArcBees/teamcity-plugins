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

import com.arcbees.vcs.VcsApi;
import com.arcbees.vcs.VcsApiFactories;
import com.arcbees.vcs.VcsConstants;
import com.arcbees.vcs.VcsPropertiesHelper;
import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.util.JsonCustomDataStorage;
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

    private final VcsApiFactories vcsApiFactories;
    private final TomcatManagerFactory tomcatManagerFactory;
    private final VcsConstants vcsConstants;
    private final Constants constants;

    public TomcatDeployHandler(VcsApiFactories vcsApiFactories,
                               TomcatManagerFactory tomcatManagerFactory,
                               VcsConstants vcsConstants,
                               Constants constants) {
        this.vcsApiFactories = vcsApiFactories;
        this.tomcatManagerFactory = tomcatManagerFactory;
        this.vcsConstants = vcsConstants;
        this.constants = constants;
    }

    public void handle(SRunningBuild build, BuildTriggerDescriptor trigger) throws IOException {
        Branch branch = build.getBranch();
        if (branch != null && build.getBuildStatus().isSuccessful()) {
            SBuildType buildType = build.getBuildType();

            VcsPropertiesHelper vcsPropertiesHelper = new VcsPropertiesHelper(trigger.getProperties(), vcsConstants);
            VcsApi vcsApi = vcsApiFactories.create(vcsPropertiesHelper);

            PullRequest pullRequest = vcsApi.getPullRequestForBranch(branch.getName());

            JsonCustomDataStorage<TomcatStagingDeploy> dataStorage = getJsonDataStorage(buildType, trigger);
            StagingPropertiesHelper stagingPropertiesHelper =
                    new StagingPropertiesHelper(trigger.getProperties(), constants);

            TomcatStagingDeploy stagingDeploy =
                    getTomcatStagingDeploy(vcsPropertiesHelper, pullRequest, dataStorage);

            deploy(build, stagingPropertiesHelper, stagingDeploy);
            postComment(vcsApi, pullRequest, stagingDeploy);

            dataStorage.putValue(getPullRequestKey(vcsPropertiesHelper, pullRequest), stagingDeploy);
        }
    }

    private void deploy(final SRunningBuild build,
                        StagingPropertiesHelper stagingPropertiesHelper,
                        final TomcatStagingDeploy stagingDeploy) throws IOException {
        final String baseContext = stagingPropertiesHelper.getBaseContext();
        final TomcatManager tomcatManager = createTomcatManager(stagingPropertiesHelper);

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

            stagingDeploy.setDeployed(true);
        } catch (Exception e) {
            stagingDeploy.setDeployed(false);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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

        String tomcatBasePath = UrlUtils.extractBaseUrl(tomcatManager.getURL());
        stagingDeploy.setWebPath(tomcatBasePath + path);

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

    private Comment postComment(VcsApi vcsApi,
                                PullRequest pullRequest,
                                TomcatStagingDeploy stagingDeploy) throws IOException {
        Comment comment = stagingDeploy.getComment();

        if (comment == null && stagingDeploy.isDeployed()) {
            comment = vcsApi.postComment(pullRequest.getId(), getComment(stagingDeploy));
            stagingDeploy.setComment(comment);
        }

        return comment;
    }

    private TomcatStagingDeploy getTomcatStagingDeploy(VcsPropertiesHelper vcsPropertiesHelper,
                                                       PullRequest pullRequest,
                                                       JsonCustomDataStorage<TomcatStagingDeploy> dataStorage) {
        String pullRequestKey = getPullRequestKey(vcsPropertiesHelper.getRepositoryOwner(),
                vcsPropertiesHelper.getRepositoryName(), pullRequest);

        TomcatStagingDeploy stagingDeploy = dataStorage.getValue(pullRequestKey);

        if (stagingDeploy == null) {
            stagingDeploy = new TomcatStagingDeploy(pullRequest, false);
        }

        return stagingDeploy;
    }

    private JsonCustomDataStorage<TomcatStagingDeploy> getJsonDataStorage(SBuildType buildType,
                                                                          BuildTriggerDescriptor trigger) {
        String storageId = getStorageId(trigger);
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(storageId);

        return JsonCustomDataStorage.create(customDataStorage, TomcatStagingDeploy.class);
    }

    private String getPullRequestKey(VcsPropertiesHelper helper, PullRequest pullRequest) {
        return getPullRequestKey(helper.getRepositoryOwner(), helper.getRepositoryName(), pullRequest);
    }

    private String getPullRequestKey(String repositoryOwner, String repositoryName, PullRequest pullRequest) {
        return vcsConstants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_" + pullRequest
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
