package com.arcbees.bitbucket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.arcbees.bitbucket.Constants;
import com.google.common.base.Strings;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

public class BitbucketPropertiesProcessor implements PropertiesProcessor {
    private final Constants constants;

    public BitbucketPropertiesProcessor(Constants constants) {
        this.constants = constants;
    }

    @Override
    public Collection<InvalidProperty> process(Map<String, String> properties) {
        Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();
        if (properties == null) return result;

        checkNotEmpty(properties, constants.getUserNameKey(), "Username must be specified", result);
        checkNotEmpty(properties, constants.getPasswordKey(), "Password must be specified", result);
        checkNotEmpty(properties, constants.getRepositoryNameKey(), "Repository name must be specified", result);
        checkNotEmpty(properties, constants.getRepositoryOwnerKey(), "Repository owner must be specified", result);

        return result;
    }

    private void checkNotEmpty(Map<String, String> properties,
                               String key,
                               String message,
                               Collection<InvalidProperty> result) {
        if (isEmptyOrSpaces(properties.get(key))) {
            result.add(new InvalidProperty(key, message));
        }
    }

    private boolean isEmptyOrSpaces(String value) {
        return Strings.nullToEmpty(value).trim().isEmpty();
    }
}
