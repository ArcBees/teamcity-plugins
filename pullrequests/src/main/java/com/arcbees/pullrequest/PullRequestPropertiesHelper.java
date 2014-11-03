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

package com.arcbees.pullrequest;

import java.util.Map;

import com.arcbees.vcs.VcsConstants;
import com.arcbees.vcs.VcsPropertiesHelper;

public class PullRequestPropertiesHelper extends VcsPropertiesHelper {
    private final Constants pullRequestConstants;

    public PullRequestPropertiesHelper(Map<String, String> properties,
                                       VcsConstants vcsConstants,
                                       Constants pullRequestConstants) {
        super(properties, vcsConstants);

        this.pullRequestConstants = pullRequestConstants;
    }

    public boolean getApproveOnSuccessKey() {
        return Boolean.parseBoolean(properties.get(pullRequestConstants.getApproveOnSuccessKey()));
    }
}
