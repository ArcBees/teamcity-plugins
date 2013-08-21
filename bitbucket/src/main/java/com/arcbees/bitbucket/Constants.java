/*
 * Copyright 2013 ArcBees Inc.
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

public class Constants {
    public String getServerUrl() {
        return "https://bitbucket.org";
    }

    public String getUserNameKey() {
        return "bitbucket_username";
    }

    public String getPasswordKey() {
        return jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX + "bitbucket_password";
    }

    public String getRepositoryNameKey() {
        return "bitbucket_repo";
    }

    public String getRepositoryOwnerKey() {
        return "bitbucket_owner";
    }

    public String getPullRequestKey() {
        return "bitbucket_pullrequest_";
    }
}
