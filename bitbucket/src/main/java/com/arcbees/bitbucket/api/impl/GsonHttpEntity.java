package com.arcbees.bitbucket.api.impl;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;

public class GsonHttpEntity extends StringEntity {
    private final String entityText;

    public GsonHttpEntity(Gson gson, Object object) throws UnsupportedEncodingException {
        this(gson.toJson(object));
    }

    public GsonHttpEntity(String entityText) throws UnsupportedEncodingException {
        super(entityText, ContentType.APPLICATION_JSON);

        this.entityText = entityText;
    }

    public String getEntityText() {
        return entityText;
    }
}
