package com.arcbees.bitbucket.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    private final Gson gson = new Gson();

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
