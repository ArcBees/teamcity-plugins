package com.arcbees.bitbucket;

import java.util.Map;

public class PropertiesHelper {
    private final Map<String, String> properties;
    private final Constants constants;

    public PropertiesHelper(Map<String, String> properties,
                            Constants constants) {
        this.properties = properties;
        this.constants = constants;
    }

    public String getRepositoryName() {
        return properties.get(constants.getRepositoryNameKey());
    }

    public String getRepositoryOwner() {
        return properties.get(constants.getRepositoryOwnerKey());
    }

    public String getPassword() {
        return properties.get(constants.getPasswordKey());
    }

    public String getUserName() {
        return properties.get(constants.getUserNameKey());
    }
}
