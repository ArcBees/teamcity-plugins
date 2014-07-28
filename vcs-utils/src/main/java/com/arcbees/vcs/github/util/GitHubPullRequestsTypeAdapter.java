/**
 * Copyright 2014 ArcBees Inc.
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

package com.arcbees.vcs.github.util;

import java.lang.reflect.Type;
import java.util.List;

import com.arcbees.vcs.github.model.GitHubPullRequest;
import com.arcbees.vcs.github.model.GitHubPullRequests;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GitHubPullRequestsTypeAdapter implements JsonDeserializer<GitHubPullRequests> {
    @Override
    public GitHubPullRequests deserialize(JsonElement json, Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        JsonArray jsonArray = json.getAsJsonArray();

        List<GitHubPullRequest> pullRequests = FluentIterable.from(jsonArray)
                .transform(new Function<JsonElement, GitHubPullRequest>() {
                    @Override
                    public GitHubPullRequest apply(JsonElement input) {
                        return context.deserialize(input, GitHubPullRequest.class);
                    }
                }).toImmutableList();

        GitHubPullRequests gitHubPullRequests = new GitHubPullRequests();
        gitHubPullRequests.setPullRequests(pullRequests);

        return gitHubPullRequests;
    }
}
