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

public class Constants {
    private static final String USERNAME_KEY = "staging_username";
    private static final String PASSWORD_KEY = jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX +
            "staging_password";
    private static final String TOMCAT_URL = "staging_manager";
    private static final String MERGE_BRANCH = "staging_branch";
    private static final String BASE_CONTEXT = "staging_context";

    public String getUserNameKey() {
        return USERNAME_KEY;
    }

    public String getPasswordKey() {
        return PASSWORD_KEY;
    }

    public String getTomcatMergeBranch() {
        return MERGE_BRANCH;
    }

    public String getTomcatUrl() {
        return TOMCAT_URL;
    }

    public String getBaseContextKey() {
        return BASE_CONTEXT;
    }
}
