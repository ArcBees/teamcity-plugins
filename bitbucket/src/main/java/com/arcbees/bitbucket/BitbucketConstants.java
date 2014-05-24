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

public class BitbucketConstants {
    private static final String BUILD_SUCCESS = "BUILD SUCCESS ";
    private static final String BUILD_FAILURE = "BUILD FAILURE ";
    private static final String BITBUCKET_URL = "https://bitbucket.org";
    private static final String USERNAME_KEY = "bitbucket_username";
    private static final String PASSWORD_KEY = jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX +
            "bitbucket_password";
    private static final String REPOSITORY_KEY = "bitbucket_repo";
    private static final String REPOSITORY_OWNER = "bitbucket_owner";
    private static final String PULLREQUEST_KEY = "bitbucket_pullrequest_";

    public String getBuildSuccess() {
        return BUILD_SUCCESS;
    }

    public String getBuildFailure() {
        return BUILD_FAILURE;
    }

    public String getServerUrl() {
        return BITBUCKET_URL;
    }

    public String getUserNameKey() {
        return USERNAME_KEY;
    }

    public String getPasswordKey() {
        return PASSWORD_KEY;
    }

    public String getRepositoryNameKey() {
        return REPOSITORY_KEY;
    }

    public String getRepositoryOwnerKey() {
        return REPOSITORY_OWNER;
    }

    public String getPullRequestKey() {
        return PULLREQUEST_KEY;
    }
}
