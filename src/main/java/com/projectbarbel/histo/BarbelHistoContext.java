package com.projectbarbel.histo;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate.UpdateExecutionContext;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.journal.functions.CGIPojoProxyingFunction;
import com.projectbarbel.histo.journal.functions.DefaultIDGenerator;
import com.projectbarbel.histo.journal.functions.DefaultVersionUpdateExecutionStrategy;
import com.projectbarbel.histo.journal.functions.GsonPojoCopier;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.Systemclock;

public interface BarbelHistoContext<T> {

    public static final String SYSTEM = "SYSTEM";
    public static final String SYSTEMACTIVITY = "SYSTEMACTIVITY";
    public static final Systemclock CLOCK = new Systemclock();
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final JsonDeserializer<ZonedDateTime> ZDT_DESERIALIZER = new JsonDeserializer<ZonedDateTime>() {
        @Override
        public ZonedDateTime deserialize(final JsonElement json, final Type typeOfT,
                final JsonDeserializationContext context) throws JsonParseException {
            return DATE_FORMATTER.parse(json.getAsString(), ZonedDateTime::from);
        }
    };
    public static final JsonSerializer<ZonedDateTime> ZDT_SERIALIZER = new JsonSerializer<ZonedDateTime>() {
        public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DATE_FORMATTER.format(src));
        }
    };

    static BarbelMode getDefaultBarbelMode() {
        return BarbelMode.POJO;
    }
    
    static <T> Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> getDefaultVersionUpdateExecutionStrategy() {
        return new DefaultVersionUpdateExecutionStrategy<T>();
    }

    static String getDefaultActivity() {
        return SYSTEMACTIVITY;
    }

    static LocalDate getInfiniteDate() {
        return LocalDate.MAX;
    }

    static Systemclock getClock() {
        return CLOCK;
    }

    static Supplier<Object> getDefaultDocumentIDGenerator() {
        return new DefaultIDGenerator();
    }

    static Supplier<Object> getDefaultVersionIDGenerator() {
        return new DefaultIDGenerator();
    }

    static String getDefaultUser() {
        return SYSTEM;
    }

    static <T> BiFunction<T, BitemporalStamp, T> getDefaultProxyingFunction() {
        return new CGIPojoProxyingFunction<T>();
    }

    static Gson getDefaultGson() {
        return new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, ZDT_DESERIALIZER)
                .registerTypeAdapter(ZonedDateTime.class, ZDT_SERIALIZER).create();
    }

    static <T> Function<T, T> getDefaultCopyFunction() {
        return new GsonPojoCopier<T>();
    }

    Supplier<?> getDocumentIdGenerator();

    Supplier<?> getVersionIdGenerator();

    IndexedCollection<T> getBackbone();

    String getActivity();

    String getUser();

    Map<Object, DocumentJournal<T>> getJournalStore();

    BiFunction<T, BitemporalStamp, T> getPojoProxyingFunction();

    Function<T, T> getPojoCopyFunction();

    Gson getGson();

    Function<BarbelHistoContext<T>, BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>>> getJournalUpdateStrategy();

    Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> getVersionUpdateExecutionStrategy();

    BarbelHistoFactory<T> getBarbelFactory();

    BarbelMode getMode();

}
