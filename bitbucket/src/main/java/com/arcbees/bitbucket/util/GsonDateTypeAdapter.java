package com.arcbees.bitbucket.util;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GsonDateTypeAdapter implements JsonDeserializer<Date> {
    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();
    private static final DateFormat CUSTOM_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");

    @Override
    public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        String value = json.getAsJsonPrimitive().getAsString();

        try {
            return DATE_FORMAT.parse(value);
        } catch (ParseException e) {
            try {
                return CUSTOM_DATE_FORMAT.parse(value);
            } catch (ParseException e1) {
                throw new JsonParseException(e1);
            }
        }
    }
}
