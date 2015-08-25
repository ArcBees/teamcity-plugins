/*
 * Copyright 2015 ArcBees Inc.
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

import java.lang.reflect.Type;

import org.junit.Test;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class GsonDateTypeAdapterTest {
    @Test
    public void deserialize() throws Exception {
        JsonElement jsonElement = mock(JsonElement.class);
        given(jsonElement.getAsJsonPrimitive()).willReturn(new JsonPrimitive("2015-07-29T17:31:29.962962+00:00"));
        GsonDateTypeAdapter typeAdapter = new GsonDateTypeAdapter();

        typeAdapter.deserialize(jsonElement, mock(Type.class), mock(JsonDeserializationContext.class));
    }
}
