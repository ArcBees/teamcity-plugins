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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.common.base.Strings;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

public class VcsPropertiesProcessor implements PropertiesProcessor {
    private final VcsConstants vcsConstants;

    public VcsPropertiesProcessor(VcsConstants vcsConstants) {
        this.vcsConstants = vcsConstants;
    }

    @Override
    public Collection<InvalidProperty> process(Map<String, String> properties) {
        Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();
        if (properties == null) return result;

        checkNotEmpty(properties, vcsConstants.getUserNameKey(), "Username must be specified", result);
        checkNotEmpty(properties, vcsConstants.getPasswordKey(), "Password must be specified", result);
        checkNotEmpty(properties, vcsConstants.getRepositoryOwnerKey(), "Repository owner must be specified", result);
        checkNotEmpty(properties, vcsConstants.getRepositoryNameKey(), "Repository name must be specified", result);

        return result;
    }

    private void checkNotEmpty(Map<String, String> properties,
                               String key,
                               String message,
                               Collection<InvalidProperty> result) {
        if (isEmptyOrSpaces(properties.get(key))) {
            result.add(new InvalidProperty(key, message));
        }
    }

    private boolean isEmptyOrSpaces(String value) {
        return Strings.nullToEmpty(value).trim().isEmpty();
    }
}
