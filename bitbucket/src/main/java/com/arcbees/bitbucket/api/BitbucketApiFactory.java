package com.arcbees.bitbucket.api;

public interface BitbucketApiFactory {
    BitbucketApi create(String userName,
                        String password);
}
