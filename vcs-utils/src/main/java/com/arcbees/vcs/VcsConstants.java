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

package com.arcbees.vcs;

import static jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX;

public class VcsConstants {
    private static final String USERNAME_KEY = "vcs_username";
    private static final String PASSWORD_KEY = SECURE_PROPERTY_PREFIX + "vcs_password";
    private static final String SERVER_URL = "vcs_server";
    private static final String REPOSITORY_KEY = "vcs_repo";
    private static final String REPOSITORY_OWNER = "vcs_owner";
    private static final String PULLREQUEST_KEY = "vcs_pullrequest_";
    private static final String VCS_TYPE = "vcs_type";

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

    public String getServerUrl() {
        return SERVER_URL;
    }

    public String getPullRequestKey() {
        return PULLREQUEST_KEY;
    }

    public String getVcsType() {
        return VCS_TYPE;
    }
}
