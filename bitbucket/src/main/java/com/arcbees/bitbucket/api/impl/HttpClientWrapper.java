package com.arcbees.bitbucket.api.impl;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public interface HttpClientWrapper {
    HttpResponse execute(HttpUriRequest request) throws IOException;
}
