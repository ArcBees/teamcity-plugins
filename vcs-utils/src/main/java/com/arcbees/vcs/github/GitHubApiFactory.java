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

package com.arcbees.vcs.github;

import com.arcbees.vcs.VcsApi;
import com.arcbees.vcs.VcsApiFactory;
import com.arcbees.vcs.VcsPropertiesHelper;
import com.arcbees.vcs.VcsType;
import com.arcbees.vcs.util.HttpClientWrapper;

public class GitHubApiFactory implements VcsApiFactory {
    private final HttpClientWrapper httpClient;
    private final GitHubVcsType gitHubVcsType;

    public GitHubApiFactory(HttpClientWrapper httpClient,
                            GitHubVcsType gitHubVcsType) {
        this.httpClient = httpClient;
        this.gitHubVcsType = gitHubVcsType;
    }

    @Override
    public boolean handles(VcsType vcsType) {
        return vcsType.equals(gitHubVcsType);
    }

    @Override
    public VcsApi create(VcsPropertiesHelper vcsPropertiesHelper) {
        return new GitHubApi(httpClient, new GitHubApiPaths(vcsPropertiesHelper.getServerUrl()),
                vcsPropertiesHelper.getUserName(),
                vcsPropertiesHelper.getPassword(),
                vcsPropertiesHelper.getRepositoryOwner(),
                vcsPropertiesHelper.getRepositoryName());
    }
}
