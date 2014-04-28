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

import java.util.Map;

public class StagingPropertiesHelper {
    private final Map<String, String> properties;
    private final Constants constants;

    public StagingPropertiesHelper(Map<String, String> properties,
                                   Constants constants) {
        this.properties = properties;
        this.constants = constants;
    }

    public String getTomcatUrl() {
        return properties.get(constants.getTomcatUrl());
    }

    public String getMergeBranch() {
        return properties.get(constants.getTomcatMergeBranch());
    }

    public String getUserName() {
        return properties.get(constants.getUserNameKey());
    }

    public String getPassword() {
        return properties.get(constants.getPasswordKey());
    }

    public String getBaseContext() {
        return properties.get(constants.getBaseContextKey());
    }
}
