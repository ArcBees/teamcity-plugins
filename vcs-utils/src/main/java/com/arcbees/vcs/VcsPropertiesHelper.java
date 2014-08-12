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

import java.util.Map;

public class VcsPropertiesHelper {
    private final Map<String, String> properties;
    private final VcsConstants vcsConstants;

    public VcsPropertiesHelper(Map<String, String> properties,
                               VcsConstants vcsConstants) {
        this.properties = properties;
        this.vcsConstants = vcsConstants;
    }

    public String getRepositoryName() {
        return properties.get(vcsConstants.getRepositoryNameKey());
    }

    public String getRepositoryOwner() {
        return properties.get(vcsConstants.getRepositoryOwnerKey());
    }

    public String getPassword() {
        return properties.get(vcsConstants.getPasswordKey());
    }

    public String getUserName() {
        return properties.get(vcsConstants.getUserNameKey());
    }

    public String getVcsType() {
        return properties.get(vcsConstants.getVcsType());
    }

    public String getServerUrl() {
        return properties.get(vcsConstants.getServerUrl());
    }

    public boolean getApproveOnSuccessKey() { return Boolean.parseBoolean(properties.get(vcsConstants.getApproveOnSuccessKey())); }

}
