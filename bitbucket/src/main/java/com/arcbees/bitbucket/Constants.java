package com.arcbees.bitbucket;

public class Constants {
    public String getServerUrl() {
        return "https://bitbucket.org";
    }

    public String getUserNameKey() {
        return "bitbucket_username";
    }

    public String getPasswordKey() {
        return jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX + "bitbucket_password";
    }

    public String getRepositoryNameKey() {
        return "bitbucket_repo";
    }

    public String getRepositoryOwnerKey() {
        return "bitbucket_owner";
    }

    public String getPullRequestKey() {
        return "bitbucket_pullrequest_";
    }
}
