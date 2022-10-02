package org.projectbarbel.histo.functions;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SimpleGsonPojoCopier implements UnaryOperator<Object> {

    final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {

                @Override
                public ZonedDateTime deserialize(JsonElement json, Type type,
                        JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                    return formatter.parse(json.getAsString(), ZonedDateTime::from);
                }
            }).registerTypeAdapter(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {

                @Override
                public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(formatter.format(src));
                }
            }).create();

    public static final SimpleGsonPojoCopier INSTANCE = new SimpleGsonPojoCopier();

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object apply(Object objectFrom) {
        return gson.fromJson(gson.toJson(objectFrom), objectFrom.getClass());
    }

}
