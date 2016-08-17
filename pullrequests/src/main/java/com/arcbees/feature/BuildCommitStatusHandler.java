/*
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.arcbees.pullrequest.BuildStatus;
import com.arcbees.pullrequest.Constants;
import com.arcbees.vcs.VcsApi;
import com.arcbees.vcs.VcsApiFactories;
import com.arcbees.vcs.VcsConstants;
import com.arcbees.vcs.VcsPropertiesHelper;
import com.arcbees.vcs.model.CommitStatus;
import com.google.common.base.Strings;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.BuildRevision;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;

public class BuildCommitStatusHandler {
    private static final Logger LOGGER = Logger.getLogger(BuildCommitStatusHandler.class.getName());

    private final VcsApiFactories vcsApiFactories;
    private final VcsConstants vcsConstants;
    private final Constants constants;
    private final WebLinks webLinks;

    public BuildCommitStatusHandler(VcsApiFactories vcsApiFactories,
            VcsConstants vcsConstants,
            Constants constants,
            WebLinks webLinks) {
        this.vcsApiFactories = vcsApiFactories;
        this.vcsConstants = vcsConstants;
        this.constants = constants;
        this.webLinks = webLinks;
    }

    public void handle(SRunningBuild build, SBuildFeatureDescriptor feature, BuildStatus buildStatus)
            throws IOException {
        LOGGER.log(Level.INFO, "Handling build status - Build Status: {0}, Branch: {1}, isSuccessful: {2}",
                new Object[]{buildStatus, build.getBranch() == null ? null : build.getBranch().getName(),
                        build.getBuildStatus().isSuccessful()});

        Branch branch = build.getBranch();
        if (branch != null) {
            VcsPropertiesHelper vcsPropertiesHelper =
                    new VcsPropertiesHelper(feature.getParameters(), vcsConstants);
            VcsApi vcsApi = vcsApiFactories.create(vcsPropertiesHelper);

            CommitStatus commitStatus = getCommitStatus(build.getBuildStatus(), buildStatus);

            updateStatus(build, vcsApi, commitStatus);
        }
    }

    private CommitStatus getCommitStatus(Status status, BuildStatus buildStatus) {
        switch (buildStatus) {
            case STARTING:
                return CommitStatus.PENDING;
            case FINISHED:
                if (status.isSuccessful()) {
                    return CommitStatus.SUCCESS;
                } else {
                    return CommitStatus.FAILURE;
                }
            default:
                return CommitStatus.ERROR;
        }
    }

    private void updateStatus(
            SRunningBuild build,
            VcsApi vcsApi,
            CommitStatus commitStatus) throws IOException {
        try {
            String statusMessage = getStatusMessage(build, commitStatus);

            List<BuildRevision> changes = getSourceCommitsHashes(build);
            for (BuildRevision change : changes) {
                String version = change.getRepositoryVersion().getVersion();
                vcsApi.updateStatus(version, statusMessage, commitStatus, getTargetUrl(build), build);
            }
        } catch (UnsupportedOperationException e) {
            // Shouldn't happen
        }
    }

    private String getStatusMessage(
            SRunningBuild build,
            CommitStatus commitStatus) {
        switch (commitStatus) {
            case ERROR:
            case FAILURE:
            case SUCCESS:
                String buildDescription = Strings.nullToEmpty(build.getStatusDescriptor().getText());

                if (!buildDescription.isEmpty()) {
                    buildDescription = " : " + buildDescription;
                }

                return build.getFullName() + buildDescription;
            case PENDING:
                return constants.getBuildStarted() + build.getFullName();
            default:
                return "";
        }
    }

    private String getTargetUrl(SRunningBuild build) {
        return webLinks.getViewResultsUrl(build);
    }

    private List<BuildRevision> getSourceCommitsHashes(SRunningBuild build) {
        List<BuildRevision> result = new ArrayList<>();
        for (BuildRevision rev : build.getRevisions()) {
            if ("jetbrains.git".equals(rev.getRoot().getVcsName())) {
                result.add(rev);
            }
        }

        return result;
    }
}
