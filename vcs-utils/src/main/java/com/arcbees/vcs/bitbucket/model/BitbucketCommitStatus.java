/*
 * Copyright 2016 ArcBees Inc.
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

package com.arcbees.vcs.bitbucket.model;

import com.arcbees.vcs.model.CommitStatus;

public class BitbucketCommitStatus {
    public enum State {
        INPROGRESS,
        SUCCESSFUL,
        FAILED;

        public static State fromCommitSatus(CommitStatus status) {
            switch (status) {
                case ERROR:
                case FAILURE:
                    return FAILED;
                case PENDING:
                    return INPROGRESS;
                case SUCCESS:
                    return SUCCESSFUL;
            }

            return FAILED;
        }
    }

    private State state;
    private String url;
    private String description;
    private String key;
    private String name;

    public BitbucketCommitStatus() {
    }

    public BitbucketCommitStatus(
            CommitStatus status,
            String key,
            String name,
            String description,
            String url) {
        this.key = key;
        this.name = name;
        this.state = State.fromCommitSatus(status);
        this.description = description;
        this.url = url;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
