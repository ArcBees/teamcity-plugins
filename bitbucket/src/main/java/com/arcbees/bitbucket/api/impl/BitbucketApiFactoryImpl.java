package com.arcbees.bitbucket.api.impl;

import com.arcbees.bitbucket.Constants;
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
    public BitbucketApi create(String userName,
                               String password,
                               String repoOwner,
                               String repoName) {
        return new BitbucketApiImpl(httpClient, new BitbucketApiPaths(constants.getServerUrl()),
                userName, password, repoOwner, repoName);
    }
}
