package com.arcbees.bitbucket;

import java.util.Map;

import com.google.common.base.Strings;

public class PropertiesHelper {
    private final Map<String, String> properties;
    private final Constants constants;

    public PropertiesHelper(Map<String, String> properties,
                            Constants constants) {
        this.properties = properties;
        this.constants = constants;
    }

    public String getRepositoryName() {
        String repoName = properties.get(constants.getRepositoryNameKey());

        return Strings.nullToEmpty(repoName).toLowerCase();
    }

    public String getRepositoryOwner() {
        String repoOwner = properties.get(constants.getRepositoryOwnerKey());

        return Strings.nullToEmpty(repoOwner).toLowerCase();
    }

    public String getPassword() {
        return properties.get(constants.getPasswordKey());
    }

    public String getUserName() {
        return properties.get(constants.getUserNameKey());
    }
}
