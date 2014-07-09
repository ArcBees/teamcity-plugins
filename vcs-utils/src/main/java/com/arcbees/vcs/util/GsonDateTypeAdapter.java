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

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GsonDateTypeAdapter implements JsonDeserializer<Date> {
    private static final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance();
    private static final DateFormat DATE_TIME_LONG_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Pattern DATE_TIME_LONG_FORMAT_PATTERN =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}(\\d{3}).\\d{2}:\\d{2}");
    private static final DateFormat CUSTOM_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
    private static final List<DateFormat> dateFormats =
            Lists.newArrayList(DATE_TIME_LONG_FORMAT, DATE_TIME_FORMAT, CUSTOM_DATE_FORMAT);

    @Override
    public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        String value = json.getAsJsonPrimitive().getAsString();
        Matcher matcher = DATE_TIME_LONG_FORMAT_PATTERN.matcher(value);

        for (DateFormat dateFormat : dateFormats) {
            try {
                if (matcher.matches()) {
                    value = value.replace(matcher.group(1), "");
                }

                return dateFormat.parse(value);
            } catch (ParseException e) {
            }
        }

        try {
            return new Date(json.getAsJsonPrimitive().getAsLong());
        } catch (NumberFormatException e) {
            throw new JsonParseException(e);
        }
    }
}
