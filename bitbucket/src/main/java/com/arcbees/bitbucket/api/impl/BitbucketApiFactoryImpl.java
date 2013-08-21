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
