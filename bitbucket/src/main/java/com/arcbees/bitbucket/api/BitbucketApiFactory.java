package com.arcbees.bitbucket.api;

import com.arcbees.bitbucket.PropertiesHelper;

public interface BitbucketApiFactory {
    BitbucketApi create(PropertiesHelper propertiesHelper);
}
