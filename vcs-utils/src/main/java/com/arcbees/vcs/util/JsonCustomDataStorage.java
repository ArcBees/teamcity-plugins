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

package com.arcbees.vcs.util;

import com.arcbees.vcs.model.Comment;
import com.arcbees.vcs.model.PullRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import jetbrains.buildServer.serverSide.CustomDataStorage;

public class JsonCustomDataStorage<T> {
    public static <T> JsonCustomDataStorage<T> create(CustomDataStorage dataStorage, Class<T> clazz) {
        return new JsonCustomDataStorage<T>(dataStorage, clazz);
    }

    public static <T> JsonCustomDataStorage<T> create(CustomDataStorage dataStorage, TypeToken<T> typeToken) {
        return new JsonCustomDataStorage<T>(dataStorage, typeToken);
    }

    private final CustomDataStorage dataStorage;
    private final Class<T> clazz;
    private final TypeToken<T> typeToken;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PullRequest.class, new PolymorphicTypeAdapter<PullRequest>())
            .registerTypeAdapter(Comment.class, new PolymorphicTypeAdapter<Comment>())
            .create();

    private JsonCustomDataStorage(CustomDataStorage dataStorage,
                                  Class<T> clazz) {
        this.dataStorage = dataStorage;
        this.clazz = clazz;
        typeToken = null;
    }

    private JsonCustomDataStorage(CustomDataStorage dataStorage,
                                  TypeToken<T> typeToken) {
        this.dataStorage = dataStorage;
        this.typeToken = typeToken;
        clazz = null;
    }

    public T getValue(String key) {
        String value = dataStorage.getValue(key);

        T object;
        try {
            if (clazz != null) {
                object = gson.fromJson(value, clazz);
            } else {
                object = gson.fromJson(value, typeToken.getType());
            }
        } catch (JsonSyntaxException e) {
            object = null;
        }

        return object;
    }

    public void putValue(String key, T object) {
        String value = gson.toJson(object);
        dataStorage.putValue(key, value);
    }
}
