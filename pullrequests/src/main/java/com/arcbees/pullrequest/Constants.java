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

public class Constants {
    private static final String BUILD_SUCCESS = "BUILD SUCCESS ";
    private static final String BUILD_FAILURE = "BUILD FAILURE ";
    private static final String BUILD_STARTED = "TeamCity Build Started : ";
    private static final String APPROVE_ON_SUCCESS_KEY = "pullrequest_approve";

    public String getBuildSuccess() {
        return BUILD_SUCCESS;
    }

    public String getBuildFailure() {
        return BUILD_FAILURE;
    }

    public String getBuildStarted() {
        return BUILD_STARTED;
    }

    public String getApproveOnSuccessKey() {
        return APPROVE_ON_SUCCESS_KEY;
    }
}
