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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
import com.arcbees.vcs.model.PullRequest;
import com.arcbees.vcs.model.PullRequestTarget;
import com.arcbees.vcs.model.PullRequests;
import com.arcbees.vcs.util.JsonCustomDataStorage;
import com.google.common.base.Strings;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;

public class TomcatStagingTrigger extends PolledBuildTrigger {
    private static final Logger LOGGER = Logger.getLogger(TomcatStagingTrigger.class.getName());

    private final VcsApiFactories vcsApiFactories;
    private final TomcatManagerFactory tomcatManagerFactory;
    private final Constants constants;
    private final VcsConstants vcsConstants;

    public TomcatStagingTrigger(VcsApiFactories vcsApiFactories,
                                TomcatManagerFactory tomcatManagerFactory,
                                Constants constants,
                                VcsConstants vcsConstants) {
        this.vcsApiFactories = vcsApiFactories;
        this.tomcatManagerFactory = tomcatManagerFactory;
        this.constants = constants;
        this.vcsConstants = vcsConstants;
    }

    @Override
    public void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
        BuildTriggerDescriptor triggerDescriptor = context.getTriggerDescriptor();
        Map<String, String> properties = triggerDescriptor.getProperties();

        StagingPropertiesHelper stagingPropertiesHelper = new StagingPropertiesHelper(properties, constants);

        String mergeBranch = stagingPropertiesHelper.getMergeBranch();

        if (!Strings.isNullOrEmpty(mergeBranch)) {
            try {
                checkBranchesToUndeploy(context, properties, stagingPropertiesHelper, mergeBranch);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void checkBranchesToUndeploy(PolledTriggerContext context, Map<String, String> properties,
                                         StagingPropertiesHelper stagingPropertiesHelper, String mergeBranch)
            throws IOException {
        VcsPropertiesHelper vcsPropertiesHelper = new VcsPropertiesHelper(properties, vcsConstants);
        String repositoryOwner = vcsPropertiesHelper.getRepositoryOwner();
        String repositoryName = vcsPropertiesHelper.getRepositoryName();

        JsonCustomDataStorage<TomcatStagingDeploy> dataStorage =
                JsonCustomDataStorage.create(context.getCustomDataStorage(), TomcatStagingDeploy.class);
        TomcatManager tomcatManager = createTomcatManager(stagingPropertiesHelper);

        PullRequests<? extends PullRequest> pullRequests = getMergedPullRequests(vcsPropertiesHelper);
        for (PullRequest pullRequest : pullRequests.getPullRequests()) {
            if (isTargetMergeBranch(mergeBranch, pullRequest)) {
                String pullRequestKey = getPullRequestKey(repositoryOwner, repositoryName, pullRequest);
                TomcatStagingDeploy stagingDeploy = getTomcatStagingDeploy(dataStorage, pullRequest, pullRequestKey);

                if (stagingDeploy.isDeployed() && !stagingDeploy.isUndeployed()) {
                    undeploy(tomcatManager, stagingDeploy);
                    dataStorage.putValue(pullRequestKey, stagingDeploy);
                }
            }
        }
    }

    private PullRequests getMergedPullRequests(VcsPropertiesHelper vcsPropertiesHelper) throws IOException {
        VcsApi vcsApi = vcsApiFactories.create(vcsPropertiesHelper);

        return vcsApi.getMergedPullRequests();
    }

    private TomcatStagingDeploy getTomcatStagingDeploy(JsonCustomDataStorage<TomcatStagingDeploy> dataStorage,
                                                       PullRequest pullRequest,
                                                       String pullRequestKey) {
        TomcatStagingDeploy stagingDeploy = dataStorage.getValue(pullRequestKey);

        if (stagingDeploy == null) {
            stagingDeploy = new TomcatStagingDeploy(pullRequest, false);
        }
        return stagingDeploy;
    }

    private TomcatManager createTomcatManager(StagingPropertiesHelper propertiesHelper) {
        try {
            return tomcatManagerFactory.create(propertiesHelper);
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new BuildTriggerException(e.getMessage(), e);
        }
    }

    private boolean isTargetMergeBranch(String mergeBranch, PullRequest pullRequest) {
        String branchName = getBranchName(pullRequest.getDestination());

        return mergeBranch.equals(branchName);
    }

    private void undeploy(TomcatManager tomcatManager, TomcatStagingDeploy stagingDeploy) throws IOException {
        boolean success;
        try {
            String webPath = getWebAppUndeployPath(tomcatManager, stagingDeploy);

            LOGGER.info("Undeploying WebAPP : " + webPath);

            TomcatManagerResponse response = tomcatManager.undeploy(webPath);
            int statusCode = response.getStatusCode();

            LOGGER.info("Undeploying Status : " + statusCode);

            success = HttpStatus.SC_OK == statusCode || HttpStatus.SC_NOT_FOUND == statusCode;
        } catch (TomcatManagerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            success = false;
        }

        stagingDeploy.setUndeployed(success);
    }

    private String getWebAppUndeployPath(TomcatManager tomcatManager,
                                         TomcatStagingDeploy stagingDeploy) {
        String tomcatUrl = UrlUtils.extractBaseUrl(tomcatManager.getURL());

        return Strings.nullToEmpty(stagingDeploy.getWebPath())
                .replace(tomcatUrl, "");
    }

    private String getBranchName(PullRequestTarget source) {
        return source.getBranch().getName();
    }

    private String getPullRequestKey(String repositoryOwner, String repositoryName, PullRequest pullRequest) {
        return vcsConstants.getPullRequestKey() + repositoryOwner + "_" + repositoryName + "_"
                + pullRequest.getId();
    }
}
