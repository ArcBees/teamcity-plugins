/**
 * Copyright 2014 ArcBees Inc.
 *
 * This file is part of Stash TeamCity plugin.
 *
 * Stash TeamCity plugin is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Stash TeamCity plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with Stash TeamCity plugin. If not,
 * see http://www.gnu.org/licenses/.
 */

package com.arcbees.vcs.stash;

import com.arcbees.vcs.VcsApi;
import com.arcbees.vcs.VcsApiFactory;
import com.arcbees.vcs.VcsPropertiesHelper;
import com.arcbees.vcs.VcsType;
import com.arcbees.vcs.util.HttpClientWrapper;

public class StashApiFactoryImpl implements VcsApiFactory {
    private final HttpClientWrapper httpClient;
    private final StashVcsType stashVcsType;

    public StashApiFactoryImpl(HttpClientWrapper httpClient,
                               StashVcsType stashVcsType) {
        this.httpClient = httpClient;
        this.stashVcsType = stashVcsType;
    }

    @Override
    public boolean handles(VcsType vcsType) {
        return vcsType.equals(stashVcsType);
    }

    @Override
    public VcsApi create(VcsPropertiesHelper vcsPropertiesHelper) {
        return new StashApiImpl(httpClient, new StashApiPaths(vcsPropertiesHelper.getServerUrl()),
                vcsPropertiesHelper.getUserName(),
                vcsPropertiesHelper.getPassword(),
                vcsPropertiesHelper.getRepositoryOwner(),
                vcsPropertiesHelper.getRepositoryName());
    }
}
