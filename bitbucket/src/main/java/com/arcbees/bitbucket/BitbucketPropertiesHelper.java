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

import java.util.Map;

public class BitbucketPropertiesHelper {
    private final Map<String, String> properties;
    private final BitbucketConstants bitbucketConstants;

    public BitbucketPropertiesHelper(Map<String, String> properties,
                                     BitbucketConstants bitbucketConstants) {
        this.properties = properties;
        this.bitbucketConstants = bitbucketConstants;
    }

    public String getRepositoryName() {
        return properties.get(bitbucketConstants.getRepositoryNameKey());
    }

    public String getRepositoryOwner() {
        return properties.get(bitbucketConstants.getRepositoryOwnerKey());
    }

    public String getPassword() {
        return properties.get(bitbucketConstants.getPasswordKey());
    }

    public String getUserName() {
        return properties.get(bitbucketConstants.getUserNameKey());
    }
}
