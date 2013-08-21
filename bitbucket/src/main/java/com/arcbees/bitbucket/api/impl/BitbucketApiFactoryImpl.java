/*
 * Copyright 2013 ArcBees Inc.
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

package com.arcbees.bitbucket.api.impl;

import com.arcbees.bitbucket.Constants;
import com.arcbees.bitbucket.PropertiesHelper;
import com.arcbees.bitbucket.api.BitbucketApi;
import com.arcbees.bitbucket.api.BitbucketApiFactory;

public class BitbucketApiFactoryImpl implements BitbucketApiFactory {
    private final HttpClientWrapper httpClient;
    private final Constants constants;

    public BitbucketApiFactoryImpl(HttpClientWrapper httpClient,
                                   Constants constants) {
        this.httpClient = httpClient;
        this.constants = constants;
    }

    @Override
    public BitbucketApi create(PropertiesHelper propertiesHelper) {
        return new BitbucketApiImpl(httpClient, new BitbucketApiPaths(constants.getServerUrl()),
                propertiesHelper.getUserName(),
                propertiesHelper.getPassword(),
                propertiesHelper.getRepositoryOwner(),
                propertiesHelper.getRepositoryName());
    }
}
